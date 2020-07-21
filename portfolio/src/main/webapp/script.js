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
     const grd = ctx.createRadialGradient(c.width/2, c.height/2, c.height/6, c.width/2 + 10, c.height/2 + 10, c.width/2);
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
     formatCanvas();
     addTextOnCanvas();
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

 function getHello() {
     fetch('/data').then(response => response.text()).then(message => {
         document.getElementById('helloSpace').innerHTML = message;
     });
 }
