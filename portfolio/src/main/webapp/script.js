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

var myKey = config.MY_KEY;

var myScript = document.createElement('script');
myScript.setAttribute('src','https://maps.googleapis.com/maps/api/js?key=' + myKey);
document.head.appendChild(myScript);

/*
 * Define the background and the characteristics of the
 * main canvas on the page
 */
 function formatCanvas() {
     //Resize canvas
     const c = document.getElementById('blankCanvas');
     c.width = window.innerWidth * 3 / 4;
     c.height = window.innerHeight * 7 / 8;

     const ctx = c.getContext('2d');

    
     //Fill in with gradient
     const grd = ctx.createRadialGradient(c.width/2, c.height/2, c.height/6, 
         c.width/2 + 10, c.height/2 + 10, c.width/2);
     grd.addColorStop(0, 'lightsteelblue');
     grd.addColorStop(1, 'white');
     
     // Fill with gradient
     ctx.fillStyle = grd;
     ctx.fillRect(0, 0, c.width, c.height);
 }

 function addTextOnCanvas() {
     const c = document.getElementById('blankCanvas');
     const ctx = c.getContext('2d');
     ctx.fillStyle = '#222';
     ctx.font = 'bold 50px Fira Mono, monospace ';
     ctx.fillText('ANDREEA NICA', c.width/2 - 200, c.height/3);

     ctx.fillStyle = '#555';
     ctx.font = '30px Fira Mono, monospace ';
     ctx.fillText('PORTOFOLIO', c.width/2  , c.height/3 + 50);
 }

/*
 * Function called when the homepage is loaded
 */
 function initBody() {
     createMap();
     formatCanvas();
     addTextOnCanvas();
     displayGalleryElements();
     getComments();
     getLoginStatus();
     fetchBlobstoreUrlAndShowForm();
 }
 
 /*
  * Make the header persistent when you scroll
  */
 window.onscroll = function() {myFunction()};
 function myFunction() {
     let myHeader = document.getElementById("myHeader");
     let sticky = myHeader.offsetTop;
     if (window.pageYOffset > sticky) {
         myHeader.classList.add("sticky");
     } else {
         myHeader.classList.remove("sticky");
     }
 }

 function GalleryElement(image, text) {
     this.image = image;
     this.text = text;
 }

 function placeImageGallery(divElement, image) {
     const img = document.createElement('IMG');
     img.setAttribute('src', image);
     img.setAttribute('style', 'max-width:90%;max-height:90%');
     divElement.appendChild(img);
 }

 function placeTextGallery(divElement, text) {
     const textnode = document.createTextNode(text);
     divElement.appendChild(textnode);
 }

 function placeButtonGallery(divElement, index) {
     if (index === 1 || index === 4) {
         const btn = document.createElement('BUTTON');
         btn.setAttribute('class', 'btn');

         if (index === 1) {
             btn.setAttribute('id', 'left');
             btn.setAttribute('onclick', 'backwardsButton()');
             btn.innerText = '<';
         } else {
             btn.setAttribute('id', 'right');
             btn.setAttribute('onclick', 'forwardButton()');
             btn.innerText = '>';
         }

         divElement.appendChild(btn);
     }
 }

 GalleryElement.prototype.placeOnPage = function(index) {
     const div = document.getElementById('item' + index);
     div.innerHTML = '';

     placeImageGallery(div, this.image);
     placeTextGallery(div, this.text);
     placeButtonGallery(div, index);
 }


 const nrElemsDisplayed = 4;
 /*
  * Text to be used as captions for photos
  */
 const text = ['I love skiing during winter', 
                'Summer is all about music festivals',
                'Rome is my favourite city!',
                'My beautiful homeland',
                'Travelling in my country',
                'My escape from the city',
                'This summer I would have visited Florence',
                'I am passionate about art',
                ];
 /*
  * Creates the collection of elements and store them in
  * allGalleryElements
  */
 let allGalleryELements = [];
 for (let i = 0; i < text.length; i++) {
     let elem = new GalleryElement('images/img' + i + '.jpg', text[i]);
     allGalleryELements.push(elem);
 }

 //Memorize the index of the first picture to be displayed
 let currNumber = 0;

 function displayGalleryElements() {
     for (var i = 0; i < nrElemsDisplayed; i++) {
         allGalleryELements[(currNumber + i) % allGalleryELements.length]
            .placeOnPage(i + 1);
     }
 }

 function forwardButton() {
     currNumber = (currNumber + 1) % allGalleryELements.length;
     displayGalleryElements();
 }
 
 function backwardsButton() {
     if (currNumber === 0) {
         currNumber = allGalleryELements.length - 1;
     } else {
         currNumber--;
     }
     displayGalleryElements();
 }

 const DEFAULT_NR_COMMENTS = 10;
 let nrCommentsDisplayed = DEFAULT_NR_COMMENTS;

/*
 * Fetch comments from the Server and display the content 
 */
 function getComments() {
     fetch('/data' + '?nrCom=' + nrCommentsDisplayed).then(response => response.json()).then(dataReceived => {
         const commentSection = document.getElementById('commentRecommendation');
         commentSection.innerHTML = '';

         let list = dataReceived.list;
         for (let i = 0; i < list.length; i++) {
             let div = createComment(list[i]);
             commentSection.appendChild(div);
         }
         
         //Show the total numbers of comments
         let labelQuantity = document.getElementById('labelQuantity');
         labelQuantity.innerText = 'out of ' +  dataReceived.lengthOfDataStore;

         //Modify the max and value attribute for the number input
         let numberInput = document.getElementById('quantity');
         numberInput.max = dataReceived.lengthOfDataStore;
         nrCommentsDisplayed = Math.min(nrCommentsDisplayed, 
                                        dataReceived.lengthOfDataStore);
         numberInput.value = nrCommentsDisplayed;
     });
 }

/*
 * Create the html element that corresponds to a comment
 */
 function createComment(comment) {
     let div = document.createElement('div');

     let myH3 = document.createElement('h3');
     myH3.innerText = getEmoji(comment.sentiment) +
        ' ' + comment.username;
     div.appendChild(myH3);

     let myH5 = document.createElement('h5');
     myH5.innerText =  comment.email;
     myH5.style = 'margin-top:-5px;'
     div.appendChild(myH5);

     let mySubject = document.createElement('p');
     mySubject.innerText = comment.subject;
     div.appendChild(mySubject);

     let deleteButtonElement = document.createElement('button');
     let buttonPicture = document.createElement('img');
     buttonPicture.src = 'images/trash.png';
     deleteButtonElement.appendChild(buttonPicture);
     deleteButtonElement.addEventListener('click', () => {
         deleteComment(comment);
         
         // Remove the task from the DOM.
         div.remove();
     });
     div.appendChild(deleteButtonElement);

     if (comment.blobKey !== undefined && comment.blobKey !== null) {
         fetch('/serveImage?blobKey=' + comment.blobKey)
         .then(response => response.blob())
         .then(myBlob => {
             let image = document.createElement('img');
             image.src = URL.createObjectURL(myBlob);
             image.style = "max-width:300px;max-height:300px";
             div.appendChild(image);
         });

         const imageAnalyseParahraph = document.createElement('p');
         imageAnalyseParahraph.innerText = 'TAGS: ' + comment.imageAnalyseResult;
         imageAnalyseParahraph.style = "color:rgb(141, 140, 140);"
         div.appendChild(imageAnalyseParahraph);
     }

     return div;
 }

 function changeNrComments() {
     const numberInput = document.getElementById('quantity');
     nrCommentsDisplayed = numberInput.value;
     getComments();
 }

/** Get emoji based on the user feelings */
 function getEmoji(sentiment) {
     if (sentiment > 0.8) {
         return String.fromCodePoint(0x1F601);
     }

     if (sentiment > 0.5) {
         return  String.fromCodePoint(0x1F604);
     }

     if (sentiment > 0.3) {
         return String.fromCodePoint(0x1F600);
     }

     if (sentiment > 0) {
         return String.fromCodePoint(0x1F642);
     }

     if (sentiment > -0.3) {
         return String.fromCodePoint(0x1F641);
     }

     if (sentiment > -0.5) {
         return String.fromCodePoint(0x1F61F);
     }

     if (sentiment > -0.8) {
         return String.fromCodePoint(0x1F61E);
     }

     return String.fromCodePoint(0x1F625);
 }

 function deleteAllComments() {
     let request = new Request('/delete-data', {method: 'POST'});
     fetch(request).then(getComments);
 }


 /** Tells the server to delete the task. */
 function deleteComment(comment) {
     const params = new URLSearchParams();
     params.append('id', comment.id);
     fetch('/delete-comment', {method: 'POST', body: params}).then(getComments);
 }

 function getLoginStatus() {
     fetch('/loginStatus')
     .then(response => response.json())
     .then(showCommentForm);
 }

/**
 * Display elements in the header and make Comment Form visible
 * depending on the authentication status
 */
 function showCommentForm(userInfo) {
     if (userInfo.isUserLoggedIn) {
         makeFormElementVisible('submitForm');
         makeFormElementVisible('usernameForm');
         makeFormElementVisible('signOutLinkHeader', userInfo.logoutUrl);

         fetch('/userInfo')
         .then(response => response.json())
         .then(user => {
             const usernameInput = document.getElementById('usernameInput');
             usernameInput.value = user.username;
         });
     } else {
         const link = document.getElementById('signUpLink');
         link.href = userInfo.loginUrl;

         makeFormElementVisible('signIn');
         makeFormElementVisible('signInLinkHeader', userInfo.loginUrl)
     }
 }

 function makeFormElementVisible(id, link) {
     const el = document.getElementById(id);
     el.hidden = false;

     if (link !== undefined) {
         el.href = link;
     }
 }

/** Function called when the user changes the username */
 function copyValueFromUsername() {
     document.getElementById("usernameChangeInput").value = 
         document.getElementById("usernameInput").value;
 }

 function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const form = document.getElementById('submitForm');
        form.action = imageUploadUrl;
        form.classList.remove('hiddenUntilLoad');
      });
}

/** Creates a map and adds it to the page. */
function createMap() {
  const map = new google.maps.Map(
      document.getElementById('map'),
      {
          center: {lat: 47.889019, lng: 3.7831301},
          zoom: 5,
          styles:[
              {
                  "elementType": "geometry",
                  "stylers": [{"color": "#1d2c4d"}]
              },
              {
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#8ec3b9"}]
              },
              {
                  "elementType": "labels.text.stroke",
                  "stylers": [{"color": "#1a3646"}]
              },
              {
                  "featureType": "administrative.country",
                  "elementType": "geometry.stroke",
                  "stylers": [{"color": "#7faa09"}, 
                              {"visibility": "on"},
                              {"weight": 1},
                              ]
              },
              {
                  "featureType": "administrative.country",
                  "elementType": "labels.text.stroke",
                  "stylers": [{"visibility": "on"}]
              },
              {
                  "featureType": "administrative.land_parcel",
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#64779e" }]
              },
              {
                  "featureType": "administrative.province",
                  "elementType": "geometry.stroke",
                  "stylers": [{"color": "#4b6878"}]
              },
              {
                  "featureType": "landscape.man_made",
                  "elementType": "geometry.stroke",
                  "stylers": [{"color": "#334e87"}]
              },
              {
                  "featureType": "landscape.natural",
                  "elementType": "geometry",
                  "stylers": [{"color": "#023e58"}]
              },
              {
                  "featureType": "poi",
                  "elementType": "geometry",
                  "stylers": [{"color": "#283d6a"}]
              },
              {
                  "featureType": "poi",
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#6f9ba5"}]
              },
              {
                  "featureType": "poi",
                  "elementType": "labels.text.stroke",
                  "stylers": [{"color": "#1d2c4d"}]
              },
              {
                  "featureType": "poi.park",
                  "elementType": "geometry.fill",
                  "stylers": [{"color": "#023e58"}]
              },
              {
                  "featureType": "poi.park",
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#3C7680"}]
              },
              {
                  "featureType": "road",
                  "elementType": "geometry",
                  "stylers": [{"color": "#304a7d"}]
              },
              {
                  "featureType": "road",
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#98a5be"}]
              },
              {
                  "featureType": "road",
                  "elementType": "labels.text.stroke",
                  "stylers": [{"color": "#1d2c4d"}]
              },
              {
                  "featureType": "road.highway",
                  "elementType": "geometry",
                  "stylers": [{"color": "#2c6675"}]
              },
              {
                  "featureType": "road.highway",
                  "elementType": "geometry.stroke",
                  "stylers": [ {"color": "#255763"}]
              },
              {
                  "featureType": "road.highway",
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#b0d5ce"}]
              },
              {
                  "featureType": "road.highway",
                  "elementType": "labels.text.stroke",
                  "stylers": [{"color": "#023e58"}]
              },
              {
                  "featureType": "transit",
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#98a5be"}]
              },
              {
                  "featureType": "transit",
                  "elementType": "labels.text.stroke",
                  "stylers": [{"color": "#1d2c4d"}]
              },
              {
                  "featureType": "transit.line",
                  "elementType": "geometry.fill",
                  "stylers": [{"color": "#283d6a"}]
              },
              {
                  "featureType": "transit.station",
                  "elementType": "geometry",
                  "stylers": [{"color": "#3a4762"}]
              },
              {
                  "featureType": "water",
                  "elementType": "geometry",
                  "stylers": [{"color": "#0e1626"}]
              },
              {
                  "featureType": "water",
                  "elementType": "labels.text.fill",
                  "stylers": [{"color": "#4e6d70"}]
              }
        ]            
    });
  map.setMapTypeId('terrain');

  const romania = {lat: 45.9432, lng: 24.9668};
  const marker = new google.maps.Marker({
    position: romania,
    icon: {
            url:'http://maps.google.com/mapfiles/kml/shapes/homegardenbusiness.png',
            size: new google.maps.Size(32, 32),
            origin: new google.maps.Point(0, 0),
            anchor: new google.maps.Point(0, 32),
            scaledSize: new google.maps.Size(32, 32),
          },
    map: map,
    title: 'My Home Country',
    animation: google.maps.Animation.DROP,
  });

  const infoWindow = new google.maps.InfoWindow({
    content: 'This is my home country',
    maxWidth: 150
  });

  marker.addListener('click', function () {
    if (marker.getAnimation() !== null) {
      marker.setAnimation(null);
    } else {
      marker.setAnimation(google.maps.Animation.BOUNCE);
    }
  });

  infoWindow.open(map, marker);
}

/** Creates a chart and add it to the page */
google.charts.load('current', {
        'packages':['geochart'],
        'mapsApiKey': 'AIzaSyAZHBan3dk4l3KU_pyTkSa1ohe85xNpazc'
      });
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
    fetch('/chocolate-data').then(response => response.json())
    .then(chocolateStatistics => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Country');
        data.addColumn('number', 'Per capita consumption in killograms');
        Object.keys(chocolateStatistics).forEach((country) => {
            data.addRow([country, chocolateStatistics[country]]);
        });

        const options = {
            'title': 'How much chocolate does we eat per year?',
            'width':700,
            'height':700,
            colorAxis: {colors: ['#FAF0E6', 'black']}
        };
        
        const chart = new google.visualization.GeoChart(
            document.getElementById('chart-container'));
            chart.draw(data, options);
    });
}
