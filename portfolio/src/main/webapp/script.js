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

/**
 * Adds a random greeting to the page.
 */

/*
    Define the background and the characteristics of the
     main canvas on the page
*/
 function formatCanvas() {
     //Resize canvas
     var c = document.getElementById("blankCanvas");
     c.width = window.innerWidth * 3 / 4;
     c.height = window.innerHeight * 7 / 8;

     var ctx = c.getContext("2d");

    
     //Fill in with gradient
     var grd = ctx.createRadialGradient(c.width/2, c.height/2, c.height/6, c.width/2 + 10, c.height/2 + 10, c.width/2);
     grd.addColorStop(0, "lightsteelblue");
     grd.addColorStop(1, "white");
     
     // Fill with gradient
     ctx.fillStyle = grd;
     ctx.fillRect(0, 0, c.width, c.height);
 }

 function addTextOnCanvas() {
     var c = document.getElementById("blankCanvas");
     var ctx = c.getContext("2d");
     ctx.fillStyle = "#222";
     ctx.font = "bold 50px Fira Mono, monospace ";
     ctx.fillText("ANDREEA NICA", c.width/2 - 200, c.height/3);

     ctx.fillStyle = "#555";
     ctx.font = "30px Fira Mono, monospace ";
     ctx.fillText("PORTOFOLIO", c.width/2  , c.height/3 + 50);
 }

/*
    Function called when the homepage is loaded
*/
 function initBody() {
     formatCanvas();
     addTextOnCanvas();
 }

 function GalleryElement(image, text) {
     this.image = image;
     this.text = text;
 }

 GalleryElement.prototype.placeOnPage = function(index) {
     var div = document.getElementById("item" + index);

     var images = div.getElementsByTagName("img");

     if (images.length === 0) {
         var img = document.createElement("IMG");
     } else {
         var img = images[0];
     }
     img.setAttribute("src", this.image);
     img.setAttribute("style", "max-width:90%;max-height:90%");

     if (images.length === 0) {
         div.appendChild(img);
     }
     
     if (this.lastTextNode !== undefined) {
         this.lastTextNode.parentNode.removeChild(this.lastTextNode);
     }

     var textnode = document.createTextNode(this.text);
     this.lastTextNode = textnode;
     div.appendChild(textnode);
 }


 const nrElemsDisplayed = 4;
 const text = ["I love skiing during winter", 
                "Summer is all about music festivals",
                "Rome is my favourite city!",
                "My beautiful homeland",
                "Travelling in my country",
                "This is my little sister"];
 /*
    Creates the collection of elements and store them in 
    allGalleryElements
 */
 let allGalleryELements = [];
 for (var i = 0; i < text.length; i++) {
     let elem = new GalleryElement("images/img" + i + ".jpg", text[i]);
     allGalleryELements.push(elem);
 }

//Memorize the first picture to be displayed
 let currNumber = 0;

 function displayGalleryElements() {
     for (var i = 0; i < nrElemsDisplayed; i++) {
         allGalleryELements[(currNumber + i) % allGalleryELements.length].placeOnPage(i + 1);
     }
 }

 function forwardButton() {
     currNumber = (currNumber + 1) % allGalleryELements.length;
     displayGalleryElements();
 }
 
 function backwardsButton() {
     if (currNumber == 0) {
         currNumber = allGalleryELements.length - 1;
     } else {
         currNumber--;
     }
     displayGalleryElements();
 }
