// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.util.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import com.google.sps.data.Comment;
import com.google.sps.data.DataSent;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    int nrCommentsDisplayed = Integer.parseInt(request.getParameter("nrCom"));

    List<Comment> database = new ArrayList<>();
    int counter = 0; //Count the number of comments sent
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String subject = (String) entity.getProperty("subject");
      long timestamp = (long) entity.getProperty("timestamp");
      String email = (String) entity.getProperty("email");
      String username = getUsername(email);
      String blobKey = (String) entity.getProperty("blobKey");
      String imageAnalyseResult = (String) entity.getProperty("imageAnalyseResult");

      Comment thisComment = new Comment(id, username, subject, email, blobKey, imageAnalyseResult);

      database.add(thisComment);
      counter++;
      if (counter == nrCommentsDisplayed) {
          break;
      }
    }

    DataSent toSend = new DataSent(results.countEntities(), database);

    Gson gson = new Gson();
    String json = gson.toJson(toSend);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

   @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      UserService userService = UserServiceFactory.getUserService();

      // Only logged-in users can submit the form
      if (userService.isUserLoggedIn()) {
          String username = request.getParameter("username");
          String subject = request.getParameter("subject");
          long timestamp = System.currentTimeMillis();
          String email = userService.getCurrentUser().getEmail();
          BlobKey blobKey = getBlobKey(request, "image");
          String blobKeyString = null;
          String imageAnalyseResult = null;

          if (blobKey != null) {
              blobKeyString = blobKey.getKeyString();
              imageAnalyseResult = getImageAnalysis(blobKey);
          }
          
          Entity comment = new Entity("Comment");
          comment.setProperty("username", username);
          comment.setProperty("subject", subject);
          comment.setProperty("timestamp", timestamp);
          comment.setProperty("email", email);
          comment.setProperty("blobKey", blobKeyString);
          comment.setProperty("imageAnalyseResult", imageAnalyseResult);
          
          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          datastore.put(comment);
          
          response.sendRedirect("/index.html#commentSection");

      }
      
  }

  String getUsername(String email) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query =
        new Query("User")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      if (entity == null) {
          return "";
      }
      String username = (String) entity.getProperty("username");
      return username;
  }
  
  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private BlobKey getBlobKey(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    if (blobInfo.getContentType().indexOf("image") == -1 ) {
        blobstoreService.delete(blobKey);
        return null;
    }
    /*
    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
    */
    return blobKey;
  }

  private String getImageAnalysis(BlobKey blobKey) throws IOException {
    byte[] blobBytes = getBlobBytes(blobKey);
    List<EntityAnnotation> imageLabels = getImageLabels(blobBytes);
    String imageAnalyseResult = "";

    for (EntityAnnotation label : imageLabels) {
      imageAnalyseResult = imageAnalyseResult  + "#" + label.getDescription() + " ";
    }

    return imageAnalyseResult;
  }

  private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

    int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
    long currentByteIndex = 0;
    boolean continueReading = true;
    while (continueReading) {
      // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
      byte[] b =
          blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
      outputBytes.write(b);

      // if we read fewer bytes than we requested, then we reached the end
      if (b.length < fetchSize) {
        continueReading = false;
      }

      currentByteIndex += fetchSize;
    }

    return outputBytes.toByteArray();
  }

  /**
   * Uses the Google Cloud Vision API to generate a list of labels that apply to the image
   * represented by the binary data stored in imgBytes.
   */
  private List<EntityAnnotation> getImageLabels(byte[] imgBytes) throws IOException {
    ByteString byteString = ByteString.copyFrom(imgBytes);
    Image image = Image.newBuilder().setContent(byteString).build();

    Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
    List<AnnotateImageRequest> requests = new ArrayList<>();
    requests.add(request);

    ImageAnnotatorClient client = ImageAnnotatorClient.create();
    BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
    client.close();
    List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
    AnnotateImageResponse imageResponse = imageResponses.get(0);

    if (imageResponse.hasError()) {
      System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
      return null;
    }

    return imageResponse.getLabelAnnotationsList();
  }

}
