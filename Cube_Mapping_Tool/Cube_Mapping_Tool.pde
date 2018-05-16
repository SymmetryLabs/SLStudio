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

//sets the cubes positions and sizes
float square1X = 48;
float square1Y = 24*22;
float square1Width = 48;
float square1Height = 48;


float square2X = 48;
float square2Y = 24*22;
float square2Width = 48;
float square2Height = 48;

float square3X = 48*1;
float square3Y = 24*22;
float square3Width = 48;
float square3Height = 48;

float square4X = 48*1;
float square4Y = 24*22;
float square4Width = 48;
float square4Height = 48;

float square5X = 48*1;
float square5Y = 24*22;
float square5Width = 48;
float square5Height = 48;

float square6X = 48*1;
float square6Y = 24*22;
float square6Width = 48;
float square6Height = 48;

float square7X = 48*1;
float square7Y = 24*22;
float square7Width = 48;
float square7Height = 48;

float square8X = 48*1;
float square8Y = 24*22;
float square8Width = 48;
float square8Height = 48;

float square9X = 48*1;
float square9Y = 24*22;
float square9Width = 48;
float square9Height = 48;

float square10X = 48*1;
float square10Y = 24*22;
float square10Width = 48;
float square10Height = 48;

float square11X = 48*1;
float square11Y = 24*22;
float square11Width = 48;
float square11Height = 48;


float square12X = 48*1;
float square12Y = 24*22;
float square12Width = 48;
float square12Height = 48;

float square13X = 48*1;
float square13Y = 24*22;
float square13Width = 48;
float square13Height = 48;

float square14X = 48*1;
float square14Y = 24*22;
float square14Width = 48;
float square14Height = 48;

float square15X = 48*1;
float square15Y = 24*22;
float square15Width = 48;
float square15Height = 48;

float square16X = 48*1;
float square16Y = 24*22;
float square16Width = 48;
float square16Height = 48;

float square17X = 48*1;
float square17Y = 24*22;
float square17Width = 48;
float square17Height = 48;

float square18X = 48*1;
float square18Y = 24*22;
float square18Width = 48;
float square18Height = 48;

float square19X = 48*1;
float square19Y = 24*22;
float square19Width = 48;
float square19Height = 48;

float square20X = 48*1;
float square20Y = 24*22;
float square20Width = 48;
float square20Height = 48;

float square21X = 48*1;
float square21Y = 24*22;
float square21Width = 48;
float square21Height = 48;

//number of cubes displayed
// int numberOfCubes = 4;

//color of save button
color rectHighlight;


Cube sugarCube;



void setup() {
  size(841, 577);
  
   
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
 sugarCube = new Cube(square20X, square20Y, 246, 0, 255, "20"); 
 sugarCube = new Cube(square19X, square19Y, 0, 114, 255, "19");  
 sugarCube = new Cube(square18X, square18Y, 101, 244, 66, "18");
 sugarCube = new Cube(square17X, square17Y, 244, 66, 66, "17");
 sugarCube = new Cube(square16X, square16Y, 246, 0, 255, "16");   
 sugarCube = new Cube(square15X, square15Y, 0, 114, 255, "15"); 
 sugarCube = new Cube(square14X, square14Y, 101, 244, 66, "14");  
 sugarCube = new Cube(square13X, square13Y, 244, 66, 66, "13");
 sugarCube = new Cube(square12X, square12Y, 246, 0, 255, "12"); 
 sugarCube = new Cube(square11X, square11Y, 0, 114, 255, "11"); 
 sugarCube = new Cube(square10X, square10Y, 101, 244, 66, "10");
 sugarCube = new Cube(square9X, square9Y, 244, 66, 66, "9");
 sugarCube = new Cube(square8X, square8Y, 246, 0, 255, "8"); 
 sugarCube = new Cube(square7X, square7Y, 0, 114, 255, "7");  
 sugarCube = new Cube(square6X, square6Y, 101, 244, 66, "6");
 sugarCube = new Cube(square5X, square5Y, 244, 66, 66, "5");
 sugarCube = new Cube(square4X, square4Y, 246, 0, 255, "4");   
 sugarCube = new Cube(square3X, square3Y, 0, 114, 255, "3"); 
 sugarCube = new Cube(square2X, square2Y, 101, 244, 66, "2");  
 sugarCube = new Cube(square1X, square1Y, 244, 66, 66, "1");


 




 



  
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
//    text("Cube A Z " + square1Y, 50, 40+525);
//    fill(101, 244, 66);
//    text("Cube B X " + square2X, 150, 20+525);
//    text("Cube B Z " + square2Y, 150, 40+525);
//    fill(0, 114, 255);
//    text("Cube C X " + square3X, 250, 20+525);
//    text("Cube C Z " + square3Y, 250, 40+525);
//    fill(246, 0, 255);
//    text("Cube D X " + square4X, 350, 20+525);
//    text("Cube D Z " + square4Y, 350, 40+525);
  


  

 
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
    Cube1MappingX = square1X/2;
    Cube1MappingY = -square1Y/2;
    Cube2MappingX = square2X/2;
    Cube2MappingY = -square2Y/2;
    Cube3MappingX = square3X/2;
    Cube3MappingY = -square3Y/2;
    Cube4MappingX = square4X/2;
    Cube4MappingY = -square4Y/2;
    Cube5MappingX = square5X/2;
    Cube5MappingY = -square5Y/2;
    Cube6MappingX = square6X/2;
    Cube6MappingY = -square6Y/2;
    Cube7MappingX = square7X/2;
    Cube7MappingY = -square7Y/2;
    Cube8MappingX = square8X/2;
    Cube8MappingY = -square8Y/2;
    Cube9MappingX = square9X/2;
    Cube9MappingY = -square9Y/2;
    Cube10MappingX = square10X/2;
    Cube10MappingY = -square10Y/2;
    Cube11MappingX = square11X/2;
    Cube11MappingY = -square11Y/2;
    Cube12MappingX = square12X/2;
    Cube12MappingY = -square12Y/2;
    Cube13MappingX = square13X/2;
    Cube13MappingY = -square13Y/2;
    Cube14MappingX = square14X/2;
    Cube14MappingY = -square14Y/2;
    Cube15MappingX = square15X/2;
    Cube15MappingY = -square15Y/2;
    Cube16MappingX = square16X/2;
    Cube16MappingY = -square16Y/2;
    Cube17MappingX = square17X/2;
    Cube17MappingY = -square17Y/2;
    Cube18MappingX = square18X/2;
    Cube18MappingY = -square18Y/2;
    Cube19MappingX = square19X/2;
    Cube19MappingY = -square19Y/2;
    Cube20MappingX = square20X/2;
    Cube20MappingY = -square20Y/2;    

 //saves the mapping x y coordinates to a text file that can later be used in the cube software for mapping
 
int Cube1MappingXint = int(Cube1MappingX);
int Cube1MappingYint = int(Cube1MappingY);
int Cube2MappingXint = int(Cube2MappingX);
int Cube2MappingYint = int(Cube2MappingY);
int Cube3MappingXint = int(Cube3MappingX);
int Cube3MappingYint = int(Cube3MappingY);
int Cube4MappingXint = int(Cube4MappingX);
int Cube4MappingYint = int(Cube4MappingY);
int Cube5MappingXint = int(Cube5MappingX);
int Cube5MappingYint = int(Cube5MappingY);
int Cube6MappingXint = int(Cube6MappingX);
int Cube6MappingYint = int(Cube6MappingY);
int Cube7MappingXint = int(Cube7MappingX);
int Cube7MappingYint = int(Cube7MappingY);
int Cube8MappingXint = int(Cube8MappingX);
int Cube8MappingYint = int(Cube8MappingY);
int Cube9MappingXint = int(Cube9MappingX);
int Cube9MappingYint = int(Cube9MappingY);
int Cube10MappingXint = int(Cube10MappingX);
int Cube10MappingYint = int(Cube10MappingY);
int Cube11MappingXint = int(Cube11MappingX);
int Cube11MappingYint = int(Cube11MappingY);
int Cube12MappingXint = int(Cube12MappingX);
int Cube12MappingYint = int(Cube12MappingY);
int Cube13MappingXint = int(Cube13MappingX);
int Cube13MappingYint = int(Cube13MappingY);
int Cube14MappingXint = int(Cube14MappingX);
int Cube14MappingYint = int(Cube14MappingY);
int Cube15MappingXint = int(Cube15MappingX);
int Cube15MappingYint = int(Cube15MappingY);
int Cube16MappingXint = int(Cube16MappingX);
int Cube16MappingYint = int(Cube16MappingY);
int Cube17MappingXint = int(Cube17MappingX);
int Cube17MappingYint = int(Cube17MappingY);
int Cube18MappingXint = int(Cube18MappingX);
int Cube18MappingYint = int(Cube18MappingY);
int Cube19MappingXint = int(Cube19MappingX);
int Cube19MappingYint = int(Cube19MappingY);
int Cube20MappingXint = int(Cube20MappingX);
int Cube20MappingYint = int(Cube20MappingY);






 String Cube1MappingXString = str(Cube1MappingXint) + " ";
 String Cube1MappingYString = str(Cube1MappingYint) + " ";
 String Cube2MappingXString = str(Cube2MappingXint) + " ";
 String Cube2MappingYString = str(Cube2MappingYint) + " ";
 String Cube3MappingXString = str(Cube3MappingXint) + " ";
 String Cube3MappingYString = str(Cube3MappingYint) + " ";
 String Cube4MappingXString = str(Cube4MappingXint) + " ";
 String Cube4MappingYString = str(Cube4MappingYint) + " ";
 String Cube5MappingXString = str(Cube5MappingXint) + " ";
 String Cube5MappingYString = str(Cube5MappingYint) + " ";
 String Cube6MappingXString = str(Cube6MappingXint) + " ";
 String Cube6MappingYString = str(Cube6MappingYint) + " ";
 String Cube7MappingXString = str(Cube7MappingXint) + " ";
 String Cube7MappingYString = str(Cube7MappingYint) + " ";
 String Cube8MappingXString = str(Cube8MappingXint) + " ";
 String Cube8MappingYString = str(Cube8MappingYint) + " ";
 String Cube9MappingXString = str(Cube9MappingXint) + " ";
 String Cube9MappingYString = str(Cube9MappingYint) + " ";
 String Cube10MappingXString = str(Cube10MappingXint)  + " ";
 String Cube10MappingYString = str(Cube10MappingYint)  + " ";
 String Cube11MappingXString = str(Cube11MappingXint)  + " ";
 String Cube11MappingYString = str(Cube11MappingYint)  + " ";
 String Cube12MappingXString = str(Cube12MappingXint)  + " ";
 String Cube12MappingYString = str(Cube12MappingYint)  + " ";
 String Cube13MappingXString = str(Cube13MappingXint)  + " ";
 String Cube13MappingYString = str(Cube13MappingYint)  + " ";
 String Cube14MappingXString = str(Cube14MappingXint)  + " ";
 String Cube14MappingYString = str(Cube14MappingYint)  + " ";
 String Cube15MappingXString = str(Cube15MappingX)  + " ";
 String Cube15MappingYString = str(Cube15MappingY)  + " ";
 String Cube16MappingXString = str(Cube16MappingX)  + " ";
 String Cube16MappingYString = str(Cube16MappingY)  + " ";
 String Cube17MappingXString = str(Cube17MappingX)  + " ";
 String Cube17MappingYString = str(Cube17MappingY)  + " ";
 String Cube18MappingXString = str(Cube18MappingX)  + " ";
 String Cube18MappingYString = str(Cube18MappingY)  + " ";
 String Cube19MappingXString = str(Cube19MappingX)  + " ";
 String Cube19MappingYString = str(Cube19MappingY)  + " ";
 String Cube20MappingXString = str(Cube20MappingX)  + " ";
 String Cube20MappingYString = str(Cube20MappingY)  + " ";


//converts the floats of the x y data into strings (saveStrings can only save data into a txt if its a string)

String Cube1MappingData = Cube1MappingXString + Cube1MappingYString + Cube2MappingXString + Cube2MappingYString + Cube3MappingXString + Cube3MappingYString 
+ Cube4MappingXString + Cube4MappingYString + Cube5MappingXString + Cube5MappingYString + Cube6MappingXString + Cube6MappingYString + Cube7MappingXString 
+ Cube7MappingYString + Cube8MappingXString + Cube8MappingYString + Cube9MappingXString + Cube9MappingYString + Cube10MappingXString + Cube10MappingYString
+ Cube11MappingXString + Cube11MappingYString + Cube12MappingXString + Cube12MappingYString + Cube13MappingXString + Cube13MappingYString + Cube14MappingXString
+ Cube14MappingYString + Cube15MappingXString + Cube15MappingYString + Cube16MappingXString + Cube16MappingYString + Cube17MappingXString + Cube17MappingYString
+ Cube18MappingXString + Cube18MappingYString + Cube19MappingXString + Cube19MappingYString + Cube20MappingXString + Cube20MappingYString;
String data = Cube1MappingData;
String[] list = split(data, ' ');




    
    
// Writes the strings to a file, each on a separate line
saveStrings("mappingfile.txt", list);
 
//closes the window after the mapping text file is saved

 showMessageDialog(null,"Mapping File Succesfully Saved!", 
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
if  (mouseX > square20X && mouseX < square20X + square20Width && mouseY > square20Y && mouseY < square20Y + square20Height) {
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
if  (mouseX > square19X && mouseX < square19X + square19Width && mouseY > square19Y && mouseY < square19Y + square19Height) {
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
if  (mouseX > square18X && mouseX < square18X + square18Width && mouseY > square18Y && mouseY < square18Y + square18Height) {
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
  if  (mouseX > square17X && mouseX < square17X + square17Width && mouseY > square17Y && mouseY < square17Y + square17Height) {
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
  if  (mouseX > square16X && mouseX < square16X + square16Width && mouseY > square16Y && mouseY < square16Y + square16Height) {
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

  if  (mouseX > square15X && mouseX < square15X + square15Width && mouseY > square15Y && mouseY < square15Y + square15Height) {
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

  if  (mouseX > square14X && mouseX < square14X + square14Width && mouseY > square14Y && mouseY < square14Y + square14Height) {
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

  if  (mouseX > square13X && mouseX < square13X + square13Width && mouseY > square13Y && mouseY < square13Y + square13Height) {
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

  if  (mouseX > square12X && mouseX < square12X + square12Width && mouseY > square12Y && mouseY < square12Y + square12Height) {
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

  if  (mouseX > square11X && mouseX < square11X + square11Width && mouseY > square11Y && mouseY < square11Y + square11Height) {
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
  if  (mouseX > square10X && mouseX < square10X + square10Width && mouseY > square10Y && mouseY < square10Y + square10Height) {
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
  if  (mouseX > square9X && mouseX < square9X + square9Width && mouseY > square9Y && mouseY < square9Y + square9Height) {
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
  if  (mouseX > square8X && mouseX < square8X + square8Width && mouseY > square8Y && mouseY < square8Y + square8Height) {

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
  if  (mouseX > square7X && mouseX < square7X + square7Width && mouseY > square7Y && mouseY < square7Y + square7Height) {
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
if (mouseX > square6X && mouseX < square6X + square6Width && mouseY > square6Y && mouseY < square6Y + square6Height) {
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
  if  (mouseX > square5X && mouseX < square5X + square5Width && mouseY > square5Y && mouseY < square5Y + square5Height) {
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

  if  (mouseX > square4X && mouseX < square4X + square4Width && mouseY > square4Y && mouseY < square4Y + square4Height) {
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
  
  if  (mouseX > square3X && mouseX < square3X + square2Width && mouseY > square3Y && mouseY < square3Y + square3Height) {
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

  if  (mouseX > square2X && mouseX < square2X + square2Width && mouseY > square2Y && mouseY < square2Y + square2Height) {
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


  if (mouseX > square1X && mouseX < square1X + square1Width && mouseY > square1Y && mouseY < square1Y + square1Height) {
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
    square1X-=square1X%24;
    square1Y-=square1Y%24;
    
    square2X-=square2X%24;
    square2Y-=square2Y%24;
    
    square3X-=square3X%24;
    square3Y-=square3Y%24;
    
    square4X-=square4X%24;
    square4Y-=square4Y%24;

    square5X-=square5X%24;
    square5Y-=square5Y%24;

    square6X-=square6X%24;
    square6Y-=square6Y%24;

    square7X-=square7X%24;
    square7Y-=square7Y%24;
    
    square8X-=square8X%24;
    square8Y-=square8Y%24;
    
    square9X-=square9X%24;
    square9Y-=square9Y%24;
    
    square10X-=square10X%24;
    square10Y-=square10Y%24;

    square11X-=square11X%24;
    square11Y-=square11Y%24;

    square12X-=square12X%24;
    square12Y-=square12Y%24;
 
    square13X-=square13X%24;
    square13Y-=square13Y%24;

    square14X-=square14X%24;
    square14Y-=square14Y%24;

    square15X-=square15X%24;
    square15Y-=square15Y%24;
    
    square16X-=square16X%24;
    square16Y-=square16Y%24;
    
    square17X-=square17X%24;
    square17Y-=square17Y%24;
    
    square18X-=square18X%24;
    square18Y-=square18Y%24;

    square19X-=square19X%24;
    square19Y-=square19Y%24;

    square20X-=square20X%24;
    square20Y-=square20Y%24;
}



// if the mouse is in the square and pressed, then move it when the mouse is dragged
public void mouseDragged() {

  if (mouseinSquare1) {
    float deltaX = mouseX - pmouseX;
    float deltaY = mouseY - pmouseY;
     square1X += deltaX;
     square1Y += deltaY;
  }
  if (mouseinSquare2) {  
    float delta2X = mouseX - pmouseX;
    float delta2Y = mouseY - pmouseY;
    square2X += delta2X;
    square2Y += delta2Y;
  }
  if (mouseinSquare3) {  
    float delta3X = mouseX - pmouseX;
    float delta3Y = mouseY - pmouseY;
    square3X += delta3X;
    square3Y += delta3Y;
  }
  if (mouseinSquare4) {  
    float delta4X = mouseX - pmouseX;
    float delta4Y = mouseY - pmouseY;
    square4X += delta4X;
    square4Y += delta4Y;
  }
  if (mouseinSquare5) {  
  float delta5X = mouseX - pmouseX;
  float delta5Y = mouseY - pmouseY;
  square5X += delta5X;
  square5Y += delta5Y;
 }
  if (mouseinSquare6) {   
  float delta6X = mouseX - pmouseX;
  float delta6Y = mouseY - pmouseY;
     square6X += delta6X;
     square6Y += delta6Y;
  }
  if (mouseinSquare7) {  
    float delta7X = mouseX - pmouseX;
    float delta7Y = mouseY - pmouseY;
    square7X += delta7X;
    square7Y += delta7Y;
  }
  if (mouseinSquare8) {  
    float delta8X = mouseX - pmouseX;
    float delta8Y = mouseY - pmouseY;
    square8X += delta8X;
    square8Y += delta8Y;
  }
  if (mouseinSquare9) {  
    float delta9X = mouseX - pmouseX;
    float delta9Y = mouseY - pmouseY;
    square9X += delta9X;
    square9Y += delta9Y;
  }
  if (mouseinSquare10) {  
    float delta10X = mouseX - pmouseX;
    float delta10Y = mouseY - pmouseY;
    square10X += delta10X;
    square10Y += delta10Y;
   }
 



if (mouseinSquare11) {
    float delta11X = mouseX - pmouseX;
    float delta11Y = mouseY - pmouseY;
     square11X += delta11X;
     square11Y += delta11Y;
  }
  if (mouseinSquare12) {  
    float delta12X = mouseX - pmouseX;
    float delta12Y = mouseY - pmouseY;
    square12X += delta12X;
    square12Y += delta12Y;
  }
  if (mouseinSquare13) {  
    float delta13X = mouseX - pmouseX;
    float delta13Y = mouseY - pmouseY;
    square13X += delta13X;
    square13Y += delta13Y;
  }
  if (mouseinSquare14) {  
    float delta14X = mouseX - pmouseX;
    float delta14Y = mouseY - pmouseY;
    square14X += delta14X;
    square14Y += delta14Y;
  }
  if (mouseinSquare15) {  
  float delta15X = mouseX - pmouseX;
  float delta15Y = mouseY - pmouseY;
  square15X += delta15X;
  square15Y += delta15Y;
 }
  if (mouseinSquare16) {   
  float delta16X = mouseX - pmouseX;
  float delta16Y = mouseY - pmouseY;
     square16X += delta16X;
     square16Y += delta16Y;
  }
  if (mouseinSquare17) {  
    float delta17X = mouseX - pmouseX;
    float delta17Y = mouseY - pmouseY;
    square17X += delta17X;
    square17Y += delta17Y;
  }
  if (mouseinSquare18) {  
    float delta18X = mouseX - pmouseX;
    float delta18Y = mouseY - pmouseY;
    square18X += delta18X;
    square18Y += delta18Y;
  }
  if (mouseinSquare19) {  
    float delta19X = mouseX - pmouseX;
    float delta19Y = mouseY - pmouseY;
    square19X += delta19X;
    square19Y += delta19Y;
  }
  if (mouseinSquare20) {  
    float delta20X = mouseX - pmouseX;
    float delta20Y = mouseY - pmouseY;
    square20X += delta20X;
    square20Y += delta20Y;
   }
  }





//creates a variable for the x y coordinates of the cube and converts it into inches (48 PIXELS = 24 INCHES)
float Cube1MappingX = square1X/24;
float Cube1MappingY = +square1Y/24;

float Cube2MappingX = square2X/24;
float Cube2MappingY = square2Y/24;

float Cube3MappingX = square3X/24;
float Cube3MappingY = square3Y/24;

float Cube4MappingX = square4X/24;
float Cube4MappingY = square4Y/24;

float Cube5MappingX = square5X/24;
float Cube5MappingY = square5Y/24;

float Cube6MappingX = square6X/24;
float Cube6MappingY = square6Y/24;

float Cube7MappingX = square7X/24;
float Cube7MappingY = square7Y/24;

float Cube8MappingX = square8X/24;
float Cube8MappingY = square8Y/24;

float Cube9MappingX = square9X/24;
float Cube9MappingY = square9Y/24;

float Cube10MappingX = square10X/24;
float Cube10MappingY = square10Y/24;

float Cube11MappingX = square11X/24;
float Cube11MappingY = square11Y/24;

float Cube12MappingX = square12X/24;
float Cube12MappingY = square12Y/24;

float Cube13MappingX = square13X/24;
float Cube13MappingY = square13Y/24;

float Cube14MappingX = square14X/24;
float Cube14MappingY = square14Y/24;

float Cube15MappingX = square15X/24;
float Cube15MappingY = square15Y/24;

float Cube16MappingX = square16X/24;
float Cube16MappingY = square16Y/24;

float Cube17MappingX = square17X/24;
float Cube17MappingY = square17Y/24;

float Cube18MappingX = square18X/24;
float Cube18MappingY = square18Y/24;

float Cube19MappingX = square19X/24;
float Cube19MappingY = square19Y/24;

float Cube20MappingX = square20X/24;
float Cube20MappingY = square20Y/24;

 