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
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

function randomQuote() {
    const quotes = [
        "To define is to limit.",
        "I could easily forgive his pride,\
         if he had not mortified mine.",
        "But how could you live and have no story to tell?",
        "If you look for perfection, you'll never be content."
    ];

    const quote = quotes[Math.floor(Math.random() * quotes.length)];

    const quoteContainer = document.getElementById('quote-container');
    quoteContainer.innerText = quote;
}
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

 function initBody() {
     formatCanvas();
     addTextOnCanvas();
 }
