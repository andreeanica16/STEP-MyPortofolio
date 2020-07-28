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
import com.google.sps.data.Comment;
import com.google.sps.data.DataSent;
import com.google.sps.data.User;

/** Manages the info of the current user */
@WebServlet("/userInfo")
public class UserServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      UserService userService = UserServiceFactory.getUserService();
      if (userService.isUserLoggedIn()) {
          String id = userService.getCurrentUser().getUserId();
          String email = userService.getCurrentUser().getEmail();
          Entity entity = getUser(id);
          if (entity == null) {
              entity = new Entity("User", id);
              entity.setProperty("id", id);
              entity.setProperty("email", email);
              //Set the default nickname substracting the ldap
              entity.setProperty("username", (email.split("@")[0]));
              
              DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
              datastore.put(entity);
          }

          User user = new User(id, email, (String) entity.getProperty("username"));
          
          Gson gson = new Gson();
          String json = gson.toJson(user);
          
          response.setContentType("application/json;");
          response.getWriter().println(json);
    }

  }

  public Entity getUser(String id) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query =
          new Query("User")
              .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      
      return entity;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      UserService userService = UserServiceFactory.getUserService();

      if (userService.isUserLoggedIn()) {
          String id = userService.getCurrentUser().getUserId();
          Entity entity = getUser(id);
          if (entity != null) {
              String username = request.getParameter("username");
              entity.setProperty("username", username);

              DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
              datastore.put(entity);

              response.sendRedirect("/index.html#commentSection");
          }
      }
  }
}
