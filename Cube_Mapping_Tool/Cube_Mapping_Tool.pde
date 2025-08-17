/*
//one square on the grid = one foot
//one cube = approx 2 feet by 2 feet
*WARNING DOUBLE HOT COFFEE* *WARNING DOUBLE HOT COFFEE* 
 ┊┊┊┊╭╯╭╯┊┊┊ ┊┊┊┊╭╯╭╯┊┊┊     ┊┊┊┊╭╯╭╯┊┊┊ ┊┊┊┊╭╯╭╯┊┊┊    
 ┊╱▔╭╯╭╯▔╲┊┊ ┊╱▔╭╯╭╯▔╲┊┊     ┊╱▔╭╯╭╯▔╲┊┊ ┊╱▔╭╯╭╯▔╲┊┊    
 ▕╲▂▂▂▂▂▂╱▏┊ ▕╲▂▂▂▂▂▂╱▏┊     ▕╲▂▂▂▂▂▂╱▏┊ ▕╲▂▂▂▂▂▂╱▏┊     
 ┊▏┈╱╲╱╲┈▕━╮ ┊▏┈╱╲╱╲┈▕━╮     ┊▏┈╱╲╱╲┈▕━╮ ┊▏┈╱╲╱╲┈▕━╮  
 ┊▏┈╲┈┈╱┈▕┊┃ ┊▏┈╲┈┈╱┈▕┊┃     ┊▏┈╲┈┈╱┈▕┊┃ ┊▏┈╲┈┈╱┈▕┊┃  
 ┊▏┈┈╲╱┈┈▕━╯ ┊▏┈┈╲╱┈┈▕━╯     ┊▏┈┈╲╱┈┈▕━╯ ┊▏┈┈╲╱┈┈▕━╯  
 ┊╲▂▂▂▂▂▂╱┊┊ ┊╲▂▂▂▂▂▂╱┊┊     ┊╲▂▂▂▂▂▂╱┊┊ ┊╲▂▂▂▂▂▂╱┊┊  
*BEGINNER PROGRAMMERS ONLY*  *BEGINNER PROGRAMMERS ONLY*
This code will allow you to map cubes by dragging the cubes to their 
placement on a stage and hitting the black box at the bottom to get the 
x y coordinates to use as the paramaters for the mapping in whatever software.
Someone should make this software better by making it so you can select the number of cubes
and rotate the cubes. 
*/


//sets the window size (dont mess with this)
//two pixels = one inch
//24 pixels = one foot
import static javax.swing.JOptionPane.*;

//make square height, width, and location into changables variables

//creates the variables that will be the coordinates for the button that saves the x y coordinates for the mapping




PImage jake;

float[] squareX = new float[20];
float[] squareY = new float[20];
float[] squarewidth = new float[20];
float[] squareheight = new float[20];




//sets the cubes positions and sizes


//number of cubes displayed
// int numberOfCubes = 4;

//color of save button
color rectHighlight;


Cube sugarCube;



void setup() {
  size(841, 577);
  for (int i = 0; i < squareX.length; i++){
  squareX[i] = 48;
  squareY[i] = 528;
  squarewidth[i] = 48;
  squareheight[i] = 48;

}
   
}


//number of lines in the sketch
int nbOfHorizontalLines = 24;
int nbOfVerticalLines = 35;

//grid numbers for the x y number grid
int xCoordinateGridNmbr = 8;
int yCoordinateGridNmbr = 21;
void draw() { 

jake = loadImage("jake.jpg");



//draws the cubes and sets the text at the bottom of the screen

  background(51);
 sugarCube = new Cube(squareX[19], squareY[19], 246, 0, 255, "20"); 
 sugarCube = new Cube(squareX[18], squareY[18], 0, 114, 255, "19");  
 sugarCube = new Cube(squareX[17], squareY[17], 101, 244, 66, "18");
 sugarCube = new Cube(squareX[16], squareY[16], 244, 66, 66, "17");
 sugarCube = new Cube(squareX[15], squareY[15], 246, 0, 255, "16");   
 sugarCube = new Cube(squareX[14], squareY[14], 0, 114, 255, "15"); 
 sugarCube = new Cube(squareX[13], squareY[13], 101, 244, 66, "14");  
 sugarCube = new Cube(squareX[12], squareY[12], 244, 66, 66, "13");
 sugarCube = new Cube(squareX[11], squareY[11], 246, 0, 255, "12"); 
 sugarCube = new Cube(squareX[10], squareY[10], 0, 114, 255, "11"); 
 sugarCube = new Cube(squareX[9], squareY[9], 101, 244, 66, "10");
 sugarCube = new Cube(squareX[8], squareY[8], 244, 66, 66, "9");
 sugarCube = new Cube(squareX[7], squareY[7], 246, 0, 255, "8"); 
 sugarCube = new Cube(squareX[6], squareY[6], 0, 114, 255, "7");  
 sugarCube = new Cube(squareX[5], squareY[5], 101, 244, 66, "6");
 sugarCube = new Cube(squareX[4], squareY[4], 244, 66, 66, "5");
 sugarCube = new Cube(squareX[3], squareY[3], 246, 0, 255, "4");   
 sugarCube = new Cube(squareX[2], squareY[2], 0, 114, 255, "3"); 
 sugarCube = new Cube(squareX[1], squareY[1], 101, 244, 66, "2");  
 sugarCube = new Cube(squareX[0], squareY[0], 244, 66, 66, "1");


 




 



  
  //draws a grid to the screen (each line represents 1 foot)
  stroke(19, 193, 176);

  float distanceBetweenHorizontalLines = (float)height/nbOfHorizontalLines;
  float distanceBetweenVerticalLines = (float)width/nbOfVerticalLines;

  for(int i = 0; i < nbOfHorizontalLines; i++)
  {
    line(0, i*distanceBetweenHorizontalLines, width, i*distanceBetweenHorizontalLines);

  }

  for(int i = 0; i < nbOfVerticalLines; i++)
  {
    line (i*distanceBetweenVerticalLines,0,i*distanceBetweenVerticalLines, height);
  }
  
  //draws jake
 rectHighlight = color(0);
 int jakex = 24;
 int jakey = 24*17;
 image(jake, jakex, jakey, 100, 100);


//draws a solid box around the x y text so its more readible
// fill(51);
// rect(48, 529, 408, 48);

  
//  fill(244, 65, 104);
//  textSize(24);
//  text ("Audience",344, 502);
//  textSize(12);
 // fill(255, 255, 255);
 // text("number of cubes " + numberOfCubes, 500, 40+515);
//    text("Cube A Z " + squareY[0], 50, 40+525);
//    fill(101, 244, 66);
//    text("Cube B X " + squareX[1], 150, 20+525);
//    text("Cube B Z " + squareY[1], 150, 40+525);
//    fill(0, 114, 255);
//    text("Cube C X " + squareX[2], 250, 20+525);
//    text("Cube C Z " + squareY[2], 250, 40+525);
//    fill(246, 0, 255);
//    text("Cube D X " + squareX[3], 350, 20+525);
//    text("Cube D Z " + squareY[3], 350, 40+525);
  


  

 
//makes the number grid
textSize(12);
fill (255, 255, 255);
text("1", 8, 16);
text("2", 8, 40);
text("3", 8, 64);
text("4", 8, 88);
text("5", 8, 112);
text("6", 8, 136);
text("7", 8, 160);
text("8", 8, 184);
text("9", 8, 208);
text("10", 6, 231);
text("11", 6, 255);
text("12", 6, 279);
text("13", 6, 303);
text("14", 6, 327);
text("15", 6, 351);
text("16", 6, 375);
text("17", 6, 399);
text("18", 6, 423);
text("19", 6, 447);
text("20", 6, 471);
text("21", 6, 495);
text("22", 6, 519);
text("23", 6, 543);
text("24", 6, 567);

text("2", 33, 16);
text("3", 57, 16);
text("4", 80, 16);
text("5", 104, 16);
text("6", 128, 16);
text("7", 152, 16);
text("8", 173, 16);
text("9", 199, 16);
text("10", 220, 16);
text("11", 244, 16);
text("12", 268, 16);
text("14", 292, 16);
text("15", 316, 16);
text("16", 340, 16);
text("17", 364, 16);
text("18", 388, 16);
text("19", 412, 16);
text("20", 436, 16);
text("21", 460, 16);
text("22", 484, 16);
text("23", 508, 16);
text("24", 532, 16);
text("24", 556, 16);
text("25", 580, 16);
text("26", 604, 16);
text("27", 628, 16);
text("28", 652, 16);
text("29", 676, 16);
text("30", 700, 16);
text("31", 724, 16);
text("32", 748, 16);
text("33", 772, 16);
text("34", 796, 16);
text("35", 820, 16);

//creates box that toggles the mapping x y text document
float x = 24;
float y = 552 ;
float w = 24;
float h = 24;


fill(rectHighlight);
rect(x,y,w,h);
fill(255, 33, 0);
textSize(10);
text ("save", x+2, y+14);


float xAddButton = 24;
float yAddButton = 528;
color addButtonColor;
addButtonColor = color(0);

//creates add cube button
// fill(addButtonColor);
// rect(xAddButton,yAddButton,w,h);
// fill(255, 33, 0);
// textSize(15);
// text ("#", xAddButton+7, yAddButton+16);




if(mouseX>x && mouseX <x+w && mouseY>y && mouseY <y+h){
rectHighlight = color(255);}

else {
rectHighlight = color(60);
}



//makes the button change colors when highlighted

if(mouseX>xAddButton && mouseX <xAddButton+w && mouseY>yAddButton && mouseY <yAddButton+h){
addButtonColor = color(255);
}

else {
addButtonColor = color(60);
}



 
 //check if mouse is pressed on the save button, if it is send the values to the variable that stores the coordinates of the mapping x y values
 if(mousePressed){
  if(mouseX>x && mouseX <x+w && mouseY>y && mouseY <y+h){
// Build one line per cube in the requested format and save it.
String[] lines = new String[20];
for (int i = 0; i < 20; i++) {
  int xOut = int(squareX[i] / 2);    // X from squareX/2
  int yOut = 0;                      // Y always 0
  int zOut = int(-squareY[i] / 2);   // Z from old Y mapping (negated)

  // Use (i+1) as the cube label
  lines[i] = "new TowerConfig(" + xOut + ", " + yOut + ", " + zOut
             + ", 0, -45, 0, new String[] { \"" + (i+1) + "\"}),";
}

saveStrings("mappingfile.txt", lines);

showMessageDialog(null, "Mapping File Successfully Saved!",
  "Congratulations", INFORMATION_MESSAGE);
  }
 else {

 } 

  }
  
 } 

  


  











//keep track of when the mouse is inside the square
boolean mouseinSquare1 = false;
boolean mouseinSquare2 = false;
boolean mouseinSquare3 = false;
boolean mouseinSquare4 = false;
boolean mouseinSquare5 = false;
boolean mouseinSquare6 = false;
boolean mouseinSquare7 = false;
boolean mouseinSquare8 = false;
boolean mouseinSquare9 = false;
boolean mouseinSquare10 = false;
boolean mouseinSquare11 = false;
boolean mouseinSquare12 = false;
boolean mouseinSquare13 = false;
boolean mouseinSquare14 = false;
boolean mouseinSquare15 = false;
boolean mouseinSquare16 = false;
boolean mouseinSquare17 = false;
boolean mouseinSquare18 = false;
boolean mouseinSquare19 = false;
boolean mouseinSquare20 = false;




//check if the mouse is in the square
void mousePressed() {
if  (mouseX > squareX[19] && mouseX < squareX[19] + squarewidth[1] && mouseY > squareY[19] && mouseY < squareY[19] + squareheight[1]) {
    mouseinSquare20 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
 }
    else {
    mouseinSquare20 = false;
}
if  (mouseX > squareX[18] && mouseX < squareX[18] + squarewidth[1] && mouseY > squareY[18] && mouseY < squareY[18] + squareheight[1]) {
    mouseinSquare19 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare19 = false;
}
if  (mouseX > squareX[17] && mouseX < squareX[17] + squarewidth[1] && mouseY > squareY[17] && mouseY < squareY[17] + squareheight[1]) {
    mouseinSquare18 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare18 = false;
}
  if  (mouseX > squareX[16] && mouseX < squareX[16] + squarewidth[1] && mouseY > squareY[16] && mouseY < squareY[16] + squareheight[1]) {
    mouseinSquare17 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare17 = false;
}
  if  (mouseX > squareX[15] && mouseX < squareX[15] + squarewidth[1] && mouseY > squareY[15] && mouseY < squareY[15] + squareheight[1]) {
    mouseinSquare16 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10 = false;   
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare16 = false;
}

  if  (mouseX > squareX[14] && mouseX < squareX[14] + squarewidth[1] && mouseY > squareY[14] && mouseY < squareY[14] + squareheight[1]) {
    mouseinSquare15 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10 = false;   
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare15 = false;
}

  if  (mouseX > squareX[13] && mouseX < squareX[13] + squarewidth[1] && mouseY > squareY[13] && mouseY < squareY[13] + squareheight[1]) {
    mouseinSquare14 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;  
 }
    else {
    mouseinSquare14 = false;
}

  if  (mouseX > squareX[12] && mouseX < squareX[12] + squarewidth[1] && mouseY > squareY[12] && mouseY < squareY[12] + squareheight[1]) {
    mouseinSquare13 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;   
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
   mouseinSquare13 = false;

}

  if  (mouseX > squareX[11] && mouseX < squareX[11] + squarewidth[1] && mouseY > squareY[11] && mouseY < squareY[11] + squareheight[1]) {
    mouseinSquare12 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;   
    mouseinSquare11= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare12 = false;
}

  if  (mouseX > squareX[10] && mouseX < squareX[10] + squarewidth[1] && mouseY > squareY[10] && mouseY < squareY[10] + squareheight[1]) {
    mouseinSquare11 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10 = false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;   
 }
    else {
    mouseinSquare11 = false;
}
  if  (mouseX > squareX[9] && mouseX < squareX[9] + squarewidth[1] && mouseY > squareY[9] && mouseY < squareY[9] + squareheight[1]) {
    mouseinSquare10 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare10 = false;
}
  if  (mouseX > squareX[8] && mouseX < squareX[8] + squarewidth[1] && mouseY > squareY[8] && mouseY < squareY[8] + squareheight[1]) {
    mouseinSquare9 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
 }
    else {
    mouseinSquare9 = false;
}
  if  (mouseX > squareX[7] && mouseX < squareX[7] + squarewidth[1] && mouseY > squareY[7] && mouseY < squareY[7] + squareheight[1]) {

    mouseinSquare8 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
  }
    else {
    mouseinSquare8 = false;
}
  if  (mouseX > squareX[6] && mouseX < squareX[6] + squarewidth[1] && mouseY > squareY[6] && mouseY < squareY[6] + squareheight[1]) {
    mouseinSquare7 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
}
    else {
    mouseinSquare7 = false;
}
if (mouseX > squareX[5] && mouseX < squareX[5] + squarewidth[1] && mouseY > squareY[5] && mouseY < squareY[5] + squareheight[1]) {
    mouseinSquare6 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
}
   else {
    mouseinSquare6 = false;
}
  if  (mouseX > squareX[4] && mouseX < squareX[4] + squarewidth[1] && mouseY > squareY[4] && mouseY < squareY[4] + squareheight[1]) {
    mouseinSquare5 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;

   }
    else {
    mouseinSquare5 = false;
}

  if  (mouseX > squareX[3] && mouseX < squareX[3] + squarewidth[1] && mouseY > squareY[3] && mouseY < squareY[3] + squareheight[1]) {
    mouseinSquare4 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
   }
    else {
    mouseinSquare4 = false;
}
  
  if  (mouseX > squareX[2] && mouseX < squareX[2] + squarewidth[1] && mouseY > squareY[2] && mouseY < squareY[2] + squareheight[1]) {
    mouseinSquare3 = true;
    mouseinSquare1 = false;
    mouseinSquare2 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;
   }
    else {
    mouseinSquare3 = false;
}

  if  (mouseX > squareX[1] && mouseX < squareX[1] + squarewidth[1] && mouseY > squareY[1] && mouseY < squareY[1] + squareheight[1]) {
    mouseinSquare2 = true;
    mouseinSquare1 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;

}
    else {
    mouseinSquare2 = false;
}


  if (mouseX > squareX[0] && mouseX < squareX[0] + squarewidth[1] && mouseY > squareY[0] && mouseY < squareY[0] + squareheight[1]) {
    mouseinSquare1 = true;
    mouseinSquare2 = false;
    mouseinSquare3 = false;
    mouseinSquare4 = false;
    mouseinSquare5 = false;
    mouseinSquare6 = false;
    mouseinSquare7 = false;
    mouseinSquare8 = false;
    mouseinSquare9 = false;
    mouseinSquare10= false;
    mouseinSquare11= false;
    mouseinSquare12= false;
    mouseinSquare13= false;
    mouseinSquare14= false;
    mouseinSquare15= false;
    mouseinSquare16= false;
    mouseinSquare17= false;
    mouseinSquare18= false;
    mouseinSquare19= false;
    mouseinSquare20= false;

  
}
    else {
    mouseinSquare1 = false;
}

}

// //snaps the cubes to the grid

void mouseReleased() {
    squareX[0]-=squareX[0]%24;
    squareY[0]-=squareY[0]%24;
    
    squareX[1]-=squareX[1]%24;
    squareY[1]-=squareY[1]%24;
    
    squareX[2]-=squareX[2]%24;
    squareY[2]-=squareY[2]%24;
    
    squareX[3]-=squareX[3]%24;
    squareY[3]-=squareY[3]%24;

    squareX[4]-=squareX[4]%24;
    squareY[4]-=squareY[4]%24;

    squareX[5]-=squareX[5]%24;
    squareY[5]-=squareY[5]%24;

    squareX[6]-=squareX[6]%24;
    squareY[6]-=squareY[6]%24;
    
    squareX[7]-=squareX[7]%24;
    squareY[7]-=squareY[7]%24;
    
    squareX[8]-=squareX[8]%24;
    squareY[8]-=squareY[8]%24;
    
    squareX[9]-=squareX[9]%24;
    squareY[9]-=squareY[9]%24;

    squareX[10]-=squareX[10]%24;
    squareY[10]-=squareY[10]%24;

    squareX[11]-=squareX[11]%24;
    squareY[11]-=squareY[11]%24;
 
    squareX[12]-=squareX[12]%24;
    squareY[12]-=squareY[12]%24;

    squareX[13]-=squareX[13]%24;
    squareY[13]-=squareY[13]%24;

    squareX[14]-=squareX[14]%24;
    squareY[14]-=squareY[14]%24;
    
    squareX[15]-=squareX[15]%24;
    squareY[15]-=squareY[15]%24;
    
    squareX[16]-=squareX[16]%24;
    squareY[16]-=squareY[16]%24;
    
    squareX[17]-=squareX[17]%24;
    squareY[17]-=squareY[17]%24;

    squareX[18]-=squareX[18]%24;
    squareY[18]-=squareY[18]%24;

    squareX[19]-=squareX[19]%24;
    squareY[19]-=squareY[19]%24;
}



// if the mouse is in the square and pressed, then move it when the mouse is dragged
public void mouseDragged() {

  if (mouseinSquare1) {
    float deltaX = mouseX - pmouseX;
    float deltaY = mouseY - pmouseY;
     squareX[0] += deltaX;
     squareY[0] += deltaY;
  }
  if (mouseinSquare2) {  
    float delta2X = mouseX - pmouseX;
    float delta2Y = mouseY - pmouseY;
    squareX[1] += delta2X;
    squareY[1] += delta2Y;
  }
  if (mouseinSquare3) {  
    float delta3X = mouseX - pmouseX;
    float delta3Y = mouseY - pmouseY;
    squareX[2] += delta3X;
    squareY[2] += delta3Y;
  }
  if (mouseinSquare4) {  
    float delta4X = mouseX - pmouseX;
    float delta4Y = mouseY - pmouseY;
    squareX[3] += delta4X;
    squareY[3] += delta4Y;
  }
  if (mouseinSquare5) {  
  float delta5X = mouseX - pmouseX;
  float delta5Y = mouseY - pmouseY;
  squareX[4] += delta5X;
  squareY[4] += delta5Y;
 }
  if (mouseinSquare6) {   
  float delta6X = mouseX - pmouseX;
  float delta6Y = mouseY - pmouseY;
     squareX[5] += delta6X;
     squareY[5] += delta6Y;
  }
  if (mouseinSquare7) {  
    float delta7X = mouseX - pmouseX;
    float delta7Y = mouseY - pmouseY;
    squareX[6] += delta7X;
    squareY[6] += delta7Y;
  }
  if (mouseinSquare8) {  
    float delta8X = mouseX - pmouseX;
    float delta8Y = mouseY - pmouseY;
    squareX[7] += delta8X;
    squareY[7] += delta8Y;
  }
  if (mouseinSquare9) {  
    float delta9X = mouseX - pmouseX;
    float delta9Y = mouseY - pmouseY;
    squareX[8] += delta9X;
    squareY[8] += delta9Y;
  }
  if (mouseinSquare10) {  
    float delta10X = mouseX - pmouseX;
    float delta10Y = mouseY - pmouseY;
    squareX[9] += delta10X;
    squareY[9] += delta10Y;
   }
 



if (mouseinSquare11) {
    float delta11X = mouseX - pmouseX;
    float delta11Y = mouseY - pmouseY;
     squareX[10] += delta11X;
     squareY[10] += delta11Y;
  }
  if (mouseinSquare12) {  
    float delta12X = mouseX - pmouseX;
    float delta12Y = mouseY - pmouseY;
    squareX[11] += delta12X;
    squareY[11] += delta12Y;
  }
  if (mouseinSquare13) {  
    float delta13X = mouseX - pmouseX;
    float delta13Y = mouseY - pmouseY;
    squareX[12] += delta13X;
    squareY[12] += delta13Y;
  }
  if (mouseinSquare14) {  
    float delta14X = mouseX - pmouseX;
    float delta14Y = mouseY - pmouseY;
    squareX[13] += delta14X;
    squareY[13] += delta14Y;
  }
  if (mouseinSquare15) {  
  float delta15X = mouseX - pmouseX;
  float delta15Y = mouseY - pmouseY;
  squareX[14] += delta15X;
  squareY[14] += delta15Y;
 }
  if (mouseinSquare16) {   
  float delta16X = mouseX - pmouseX;
  float delta16Y = mouseY - pmouseY;
     squareX[15] += delta16X;
     squareY[15] += delta16Y;
  }
  if (mouseinSquare17) {  
    float delta17X = mouseX - pmouseX;
    float delta17Y = mouseY - pmouseY;
    squareX[16] += delta17X;
    squareY[16] += delta17Y;
  }
  if (mouseinSquare18) {  
    float delta18X = mouseX - pmouseX;
    float delta18Y = mouseY - pmouseY;
    squareX[17] += delta18X;
    squareY[17] += delta18Y;
  }
  if (mouseinSquare19) {  
    float delta19X = mouseX - pmouseX;
    float delta19Y = mouseY - pmouseY;
    squareX[18] += delta19X;
    squareY[18] += delta19Y;
  }
  if (mouseinSquare20) {  
    float delta20X = mouseX - pmouseX;
    float delta20Y = mouseY - pmouseY;
    squareX[19] += delta20X;
    squareY[19] += delta20Y;
   }
  }





//creates a variable for the x y coordinates of the cube and converts it into inches (48 PIXELS = 24 INCHES)
float Cube1MappingX = squareX[0]/24;
float Cube1MappingY = +squareY[0]/24;

float Cube2MappingX = squareX[1]/24;
float Cube2MappingY = squareY[1]/24;

float Cube3MappingX = squareX[2]/24;
float Cube3MappingY = squareY[2]/24;

float Cube4MappingX = squareX[3]/24;
float Cube4MappingY = squareY[3]/24;

float Cube5MappingX = squareX[4]/24;
float Cube5MappingY = squareY[4]/24;

float Cube6MappingX = squareX[5]/24;
float Cube6MappingY = squareY[5]/24;

float Cube7MappingX = squareX[6]/24;
float Cube7MappingY = squareY[6]/24;

float Cube8MappingX = squareX[7]/24;
float Cube8MappingY = squareY[7]/24;

float Cube9MappingX = squareX[8]/24;
float Cube9MappingY = squareY[8]/24;

float Cube10MappingX = squareX[9]/24;
float Cube10MappingY = squareY[9]/24;

float Cube11MappingX = squareX[10]/24;
float Cube11MappingY = squareY[10]/24;

float Cube12MappingX = squareX[11]/24;
float Cube12MappingY = squareY[11]/24;

float Cube13MappingX = squareX[12]/24;
float Cube13MappingY = squareY[12]/24;

float Cube14MappingX = squareX[13]/24;
float Cube14MappingY = squareY[13]/24;

float Cube15MappingX = squareX[14]/24;
float Cube15MappingY = squareY[14]/24;

float Cube16MappingX = squareX[15]/24;
float Cube16MappingY = squareY[15]/24;

float Cube17MappingX = squareX[16]/24;
float Cube17MappingY = squareY[16]/24;

float Cube18MappingX = squareX[17]/24;
float Cube18MappingY = squareY[17]/24;

float Cube19MappingX = squareX[18]/24;
float Cube19MappingY = squareY[18]/24;

float Cube20MappingX = squareX[19]/24;
float Cube20MappingY = squareY[19]/24;

 
