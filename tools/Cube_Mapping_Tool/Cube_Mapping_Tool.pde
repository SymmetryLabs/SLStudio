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
    

    // Convert coordinates for TowerConfig format
    Cube1MappingX = squareX[0]/2;
    Cube1MappingZ = -squareY[0]/2;
    Cube2MappingX = squareX[1]/2;
    Cube2MappingZ = -squareY[1]/2;
    Cube3MappingX = squareX[2]/2;
    Cube3MappingZ = -squareY[2]/2;
    Cube4MappingX = squareX[3]/2;
    Cube4MappingZ = -squareY[3]/2;
    Cube5MappingX = squareX[4]/2;
    Cube5MappingZ = -squareY[4]/2;
    Cube6MappingX = squareX[5]/2;
    Cube6MappingZ = -squareY[5]/2;
    Cube7MappingX = squareX[6]/2;
    Cube7MappingZ = -squareY[6]/2;
    Cube8MappingX = squareX[7]/2;
    Cube8MappingZ = -squareY[7]/2;
    Cube9MappingX = squareX[8]/2;
    Cube9MappingZ = -squareY[8]/2;
    Cube10MappingX = squareX[9]/2;
    Cube10MappingZ = -squareY[9]/2;
    Cube11MappingX = squareX[10]/2;
    Cube11MappingZ = -squareY[10]/2;
    Cube12MappingX = squareX[11]/2;
    Cube12MappingZ = -squareY[11]/2;
    Cube13MappingX = squareX[12]/2;
    Cube13MappingZ = -squareY[12]/2;
    Cube14MappingX = squareX[13]/2;
    Cube14MappingZ = -squareY[13]/2;
    Cube15MappingX = squareX[14]/2;
    Cube15MappingZ = -squareY[14]/2;
    Cube16MappingX = squareX[15]/2;
    Cube16MappingZ = -squareY[15]/2;
    Cube17MappingX = squareX[16]/2;
    Cube17MappingZ = -squareY[16]/2;
    Cube18MappingX = squareX[17]/2;
    Cube18MappingZ = -squareY[17]/2;
    Cube19MappingX = squareX[18]/2;
    Cube19MappingZ = -squareY[18]/2;
    Cube20MappingX = squareX[19]/2;
    Cube20MappingZ = -squareY[19]/2;    

 //saves the mapping x y coordinates to a text file that can later be used in the cube software for mapping
 
// Old variable declarations - replaced with TowerConfig format
int Cube1MappingXint = int(Cube1MappingX);
int Cube1MappingZint = int(Cube1MappingZ);
int Cube2MappingXint = int(Cube2MappingX);
int Cube2MappingZint = int(Cube2MappingZ);
int Cube3MappingXint = int(Cube3MappingX);
int Cube3MappingZint = int(Cube3MappingZ);
int Cube4MappingXint = int(Cube4MappingX);
int Cube4MappingZint = int(Cube4MappingZ);
int Cube5MappingXint = int(Cube5MappingX);
int Cube5MappingZint = int(Cube5MappingZ);
int Cube6MappingXint = int(Cube6MappingX);
int Cube6MappingZint = int(Cube6MappingZ);
int Cube7MappingXint = int(Cube7MappingX);
int Cube7MappingZint = int(Cube7MappingZ);
int Cube8MappingXint = int(Cube8MappingX);
int Cube8MappingZint = int(Cube8MappingZ);
int Cube9MappingXint = int(Cube9MappingX);
int Cube9MappingZint = int(Cube9MappingZ);
int Cube10MappingXint = int(Cube10MappingX);
int Cube10MappingZint = int(Cube10MappingZ);
int Cube11MappingXint = int(Cube11MappingX);
int Cube11MappingZint = int(Cube11MappingZ);
int Cube12MappingXint = int(Cube12MappingX);
int Cube12MappingZint = int(Cube12MappingZ);
int Cube13MappingXint = int(Cube13MappingX);
int Cube13MappingZint = int(Cube13MappingZ);
int Cube14MappingXint = int(Cube14MappingX);
int Cube14MappingZint = int(Cube14MappingZ);
int Cube15MappingXint = int(Cube15MappingX);
int Cube15MappingZint = int(Cube15MappingZ);
int Cube16MappingXint = int(Cube16MappingX);
int Cube16MappingZint = int(Cube16MappingZ);
int Cube17MappingXint = int(Cube17MappingX);
int Cube17MappingZint = int(Cube17MappingZ);
int Cube18MappingXint = int(Cube18MappingX);
int Cube18MappingZint = int(Cube18MappingZ);
int Cube19MappingXint = int(Cube19MappingX);
int Cube19MappingZint = int(Cube19MappingZ);
int Cube20MappingXint = int(Cube20MappingX);
int Cube20MappingZint = int(Cube20MappingZ);

// Create TowerConfig strings for each cube
String Cube1Config = "new TowerConfig(" + str(Cube1MappingXint) + ", 0, " + str(Cube1MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube2Config = "new TowerConfig(" + str(Cube2MappingXint) + ", 0, " + str(Cube2MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube3Config = "new TowerConfig(" + str(Cube3MappingXint) + ", 0, " + str(Cube3MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube4Config = "new TowerConfig(" + str(Cube4MappingXint) + ", 0, " + str(Cube4MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube5Config = "new TowerConfig(" + str(Cube5MappingXint) + ", 0, " + str(Cube5MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube6Config = "new TowerConfig(" + str(Cube6MappingXint) + ", 0, " + str(Cube6MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube7Config = "new TowerConfig(" + str(Cube7MappingXint) + ", 0, " + str(Cube7MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube8Config = "new TowerConfig(" + str(Cube8MappingXint) + ", 0, " + str(Cube8MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube9Config = "new TowerConfig(" + str(Cube9MappingXint) + ", 0, " + str(Cube9MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube10Config = "new TowerConfig(" + str(Cube10MappingXint) + ", 0, " + str(Cube10MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube11Config = "new TowerConfig(" + str(Cube11MappingXint) + ", 0, " + str(Cube11MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube12Config = "new TowerConfig(" + str(Cube12MappingXint) + ", 0, " + str(Cube12MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube13Config = "new TowerConfig(" + str(Cube13MappingXint) + ", 0, " + str(Cube13MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube14Config = "new TowerConfig(" + str(Cube14MappingXint) + ", 0, " + str(Cube14MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube15Config = "new TowerConfig(" + str(Cube15MappingXint) + ", 0, " + str(Cube15MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube16Config = "new TowerConfig(" + str(Cube16MappingXint) + ", 0, " + str(Cube16MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube17Config = "new TowerConfig(" + str(Cube17MappingXint) + ", 0, " + str(Cube17MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube18Config = "new TowerConfig(" + str(Cube18MappingXint) + ", 0, " + str(Cube18MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube19Config = "new TowerConfig(" + str(Cube19MappingXint) + ", 0, " + str(Cube19MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";
String Cube20Config = "new TowerConfig(" + str(Cube20MappingXint) + ", 0, " + str(Cube20MappingZint) + ", 0, -45, 0, new String[] { \"\" }),";

// Create array of TowerConfig strings for each cube
String[] list = {
  Cube1Config,
  Cube2Config,
  Cube3Config,
  Cube4Config,
  Cube5Config,
  Cube6Config,
  Cube7Config,
  Cube8Config,
  Cube9Config,
  Cube10Config,
  Cube11Config,
  Cube12Config,
  Cube13Config,
  Cube14Config,
  Cube15Config,
  Cube16Config,
  Cube17Config,
  Cube18Config,
  Cube19Config,
  Cube20Config
};

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
// Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX20 = squareX[19] + squarewidth[1]/2;
    float centerY20 = squareY[19] + squareheight[1]/2;
    float dx20 = abs(mouseX - centerX20);
    float dy20 = abs(mouseY - centerY20);
    if (dx20 + dy20 <= squarewidth[1]/2) {
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
// Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX19 = squareX[18] + squarewidth[1]/2;
    float centerY19 = squareY[18] + squareheight[1]/2;
    float dx19 = abs(mouseX - centerX19);
    float dy19 = abs(mouseY - centerY19);
    if (dx19 + dy19 <= squarewidth[1]/2) {
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
// Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX18 = squareX[17] + squarewidth[1]/2;
    float centerY18 = squareY[17] + squareheight[1]/2;
    float dx18 = abs(mouseX - centerX18);
    float dy18 = abs(mouseY - centerY18);
    if (dx18 + dy18 <= squarewidth[1]/2) {
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
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX17 = squareX[16] + squarewidth[1]/2;
    float centerY17 = squareY[16] + squareheight[1]/2;
    float dx17 = abs(mouseX - centerX17);
    float dy17 = abs(mouseY - centerY17);
    if (dx17 + dy17 <= squarewidth[1]/2) {
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
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX16 = squareX[15] + squarewidth[1]/2;
    float centerY16 = squareY[15] + squareheight[1]/2;
    float dx16 = abs(mouseX - centerX16);
    float dy16 = abs(mouseY - centerY16);
    if (dx16 + dy16 <= squarewidth[1]/2) {
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

  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX15 = squareX[14] + squarewidth[14]/2;
    float centerY15 = squareY[14] + squareheight[14]/2;
    float dx15 = abs(mouseX - centerX15);
    float dy15 = abs(mouseY - centerY15);
    if (dx15 + dy15 <= squarewidth[1]/2) {
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

  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX14 = squareX[13] + squarewidth[1]/2;
    float centerY14 = squareY[13] + squareheight[1]/2;
    float dx14 = abs(mouseX - centerX14);
    float dy14 = abs(mouseY - centerY14);
    if (dx14 + dy14 <= squarewidth[1]/2) {
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

  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX13 = squareX[12] + squarewidth[1]/2;
    float centerY13 = squareY[12] + squareheight[1]/2;
    float dx13 = abs(mouseX - centerX13);
    float dy13 = abs(mouseY - centerY13);
    if (dx13 + dy13 <= squarewidth[1]/2) {
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

  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX12 = squareX[11] + squarewidth[1]/2;
    float centerY12 = squareY[11] + squareheight[1]/2;
    float dx12 = abs(mouseX - centerX12);
    float dy12 = abs(mouseY - centerY12);
    if (dx12 + dy12 <= squarewidth[1]/2) {
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

  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX11 = squareX[10] + squarewidth[1]/2;
    float centerY11 = squareY[10] + squareheight[1]/2;
    float dx11 = abs(mouseX - centerX11);
    float dy11 = abs(mouseY - centerY11);
    if (dx11 + dy11 <= squarewidth[1]/2) {
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
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX10 = squareX[9] + squarewidth[1]/2;
    float centerY10 = squareY[9] + squareheight[1]/2;
    float dx10 = abs(mouseX - centerX10);
    float dy10 = abs(mouseY - centerY10);
    if (dx10 + dy10 <= squarewidth[1]/2) {
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
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX9 = squareX[8] + squarewidth[1]/2;
    float centerY9 = squareY[8] + squareheight[1]/2;
    float dx9 = abs(mouseX - centerX9);
    float dy9 = abs(mouseY - centerY9);
    if (dx9 + dy9 <= squarewidth[1]/2) {
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
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX8 = squareX[7] + squarewidth[1]/2;
    float centerY8 = squareY[7] + squareheight[1]/2;
    float dx8 = abs(mouseX - centerX8);
    float dy8 = abs(mouseY - centerY8);
    if (dx8 + dy8 <= squarewidth[1]/2) {
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
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX7 = squareX[6] + squarewidth[1]/2;
    float centerY7 = squareY[6] + squareheight[1]/2;
    float dx7 = abs(mouseX - centerX7);
    float dy7 = abs(mouseY - centerY7);
    if (dx7 + dy7 <= squarewidth[1]/2) {
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
// Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX6 = squareX[5] + squarewidth[1]/2;
    float centerY6 = squareY[5] + squareheight[1]/2;
    float dx6 = abs(mouseX - centerX6);
    float dy6 = abs(mouseY - centerY6);
    if (dx6 + dy6 <= squarewidth[1]/2) {
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
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX5 = squareX[4] + squarewidth[1]/2;
    float centerY5 = squareY[4] + squareheight[1]/2;
    float dx5 = abs(mouseX - centerX5);
    float dy5 = abs(mouseY - centerY5);
    if (dx5 + dy5 <= squarewidth[1]/2) {
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

  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX4 = squareX[3] + squarewidth[1]/2;
    float centerY4 = squareY[3] + squareheight[1]/2;
    float dx4 = abs(mouseX - centerX4);
    float dy4 = abs(mouseY - centerY4);
    if (dx4 + dy4 <= squarewidth[1]/2) {
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
  
  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX3 = squareX[2] + squarewidth[1]/2;
    float centerY3 = squareY[2] + squareheight[1]/2;
    float dx3 = abs(mouseX - centerX3);
    float dy3 = abs(mouseY - centerY3);
    if (dx3 + dy3 <= squarewidth[1]/2) {
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

  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX2 = squareX[1] + squarewidth[1]/2;
    float centerY2 = squareY[1] + squareheight[1]/2;
    float dx2 = abs(mouseX - centerX2);
    float dy2 = abs(mouseY - centerY2);
    if (dx2 + dy2 <= squarewidth[1]/2) {
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


  // Check if mouse is inside diamond shape (rotated 45 degrees)
    float centerX1 = squareX[0] + squarewidth[1]/2;
    float centerY1 = squareY[0] + squareheight[1]/2;
    float dx1 = abs(mouseX - centerX1);
    float dy1 = abs(mouseY - centerY1);
    if (dx1 + dy1 <= squarewidth[1]/2) {
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

//snaps the cubes to the grid

void mouseReleased() {
    // Snap diamonds to 17-pixel grid for proper half-cube diagonal spacing
    // For 45° diamonds: 24 pixels / √2 ≈ 17 pixels
    float diamondSpacing = 17;
    
    squareX[0]-=squareX[0]%diamondSpacing;
    squareY[0]-=squareY[0]%diamondSpacing;
    
    squareX[1]-=squareX[1]%diamondSpacing;
    squareY[1]-=squareY[1]%diamondSpacing;
    
    squareX[2]-=squareX[2]%diamondSpacing;
    squareY[2]-=squareY[2]%diamondSpacing;
    
    squareX[3]-=squareX[3]%diamondSpacing;
    squareY[3]-=squareY[3]%diamondSpacing;

    squareX[4]-=squareX[4]%diamondSpacing;
    squareY[4]-=squareY[4]%diamondSpacing;

    squareX[5]-=squareX[5]%diamondSpacing;
    squareY[5]-=squareY[5]%diamondSpacing;

    squareX[6]-=squareX[6]%diamondSpacing;
    squareY[6]-=squareY[6]%diamondSpacing;
    
    squareX[7]-=squareX[7]%diamondSpacing;
    squareY[7]-=squareY[7]%diamondSpacing;
    
    squareX[8]-=squareX[8]%diamondSpacing;
    squareY[8]-=squareY[8]%diamondSpacing;
    
    squareX[9]-=squareX[9]%diamondSpacing;
    squareY[9]-=squareY[9]%diamondSpacing;

    squareX[10]-=squareX[10]%diamondSpacing;
    squareY[10]-=squareY[10]%diamondSpacing;

    squareX[11]-=squareX[11]%diamondSpacing;
    squareY[11]-=squareY[11]%diamondSpacing;
 
    squareX[12]-=squareX[12]%diamondSpacing;
    squareY[12]-=squareY[12]%diamondSpacing;

    squareX[13]-=squareX[13]%diamondSpacing;
    squareY[13]-=squareY[13]%diamondSpacing;

    squareX[14]-=squareX[14]%diamondSpacing;
    squareY[14]-=squareY[14]%diamondSpacing;
    
    squareX[15]-=squareX[15]%diamondSpacing;
    squareY[15]-=squareY[15]%diamondSpacing;
    
    squareX[16]-=squareX[16]%diamondSpacing;
    squareY[16]-=squareY[16]%diamondSpacing;
    
    squareX[17]-=squareX[17]%diamondSpacing;
    squareY[17]-=squareY[17]%diamondSpacing;

    squareX[18]-=squareX[18]%diamondSpacing;
    squareY[18]-=squareY[18]%diamondSpacing;

    squareX[19]-=squareX[19]%diamondSpacing;
    squareY[19]-=squareY[19]%diamondSpacing;
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





//creates a variable for the x z coordinates of the cube and converts it into inches (48 PIXELS = 24 INCHES)
float Cube1MappingX = squareX[0]/24;
float Cube1MappingZ = +squareY[0]/24;

float Cube2MappingX = squareX[1]/24;
float Cube2MappingZ = squareY[1]/24;

float Cube3MappingX = squareX[2]/24;
float Cube3MappingZ = squareY[2]/24;

float Cube4MappingX = squareX[3]/24;
float Cube4MappingZ = squareY[3]/24;

float Cube5MappingX = squareX[4]/24;
float Cube5MappingZ = squareY[4]/24;

float Cube6MappingX = squareX[5]/24;
float Cube6MappingZ = squareY[5]/24;

float Cube7MappingX = squareX[6]/24;
float Cube7MappingZ = squareY[6]/24;

float Cube8MappingX = squareX[7]/24;
float Cube8MappingZ = squareY[7]/24;

float Cube9MappingX = squareX[8]/24;
float Cube9MappingZ = squareY[8]/24;

float Cube10MappingX = squareX[9]/24;
float Cube10MappingZ = squareY[9]/24;

float Cube11MappingX = squareX[10]/24;
float Cube11MappingZ = squareY[10]/24;

float Cube12MappingX = squareX[11]/24;
float Cube12MappingZ = squareY[11]/24;

float Cube13MappingX = squareX[12]/24;
float Cube13MappingZ = squareY[12]/24;

float Cube14MappingX = squareX[13]/24;
float Cube14MappingZ = squareY[13]/24;

float Cube15MappingX = squareX[14]/24;
float Cube15MappingZ = squareY[14]/24;

float Cube16MappingX = squareX[15]/24;
float Cube16MappingZ = squareY[15]/24;

float Cube17MappingX = squareX[16]/24;
float Cube17MappingZ = squareY[16]/24;

float Cube18MappingX = squareX[17]/24;
float Cube18MappingZ = squareY[17]/24;

float Cube19MappingX = squareX[18]/24;
float Cube19MappingZ = squareY[18]/24;

float Cube20MappingX = squareX[19]/24;
float Cube20MappingZ = squareY[19]/24;

 