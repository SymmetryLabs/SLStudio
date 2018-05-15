public class Cube {
  float CubeWidth = 48;
  float CubeHeight = 48;
  float square1X = 200;
  float square1Y = 50;

Cube(float tempsquareX, float tempsquareY, int color1, int color2, int color3, String cubeNumber){

String tempCubeNumber= cubeNumber;
square1X = tempsquareX;
square1Y = tempsquareY;

int tempcolor1 = color1;
int tempcolor2 = color2;
int tempcolor3 = color3;

color1 = tempcolor1;
color2 = tempcolor2;
color3 = tempcolor3;
fill(tempcolor1, tempcolor2, tempcolor3); 
rect(square1X, square1Y, CubeWidth, CubeHeight);
fill(0,0,0);
text(tempCubeNumber, square1X+4, square1Y+14);

 


}





boolean mouseinSquare1 = false;
boolean mouseinSquare2 = false;
boolean mouseinSquare3 = false;
boolean mouseinSquare4 = false;
boolean mouseinSquare5 = false;
boolean mouseinSquare6 = false;

}

//check if the mouse is in the square
//void mousePressed() {
//  if (mouseX > square1X && mouseX < square1X + square1Width && mouseY > square1Y && mouseY < square1Y + square1Height) {
//    mouseinSquare1 = true;
//    mouseinSquare2 = false;
//    mouseinSquare3 = false;
//    mouseinSquare4 = false;
//    mouseinSquare5 = false;

//}
  
//  else {
//    mouseinSquare1 = false;
//}

//  if  (mouseX > square2X && mouseX < square2X + square2Width && mouseY > square2Y && mouseY < square2Y + square2Height) {
//    mouseinSquare2 = true;
//    mouseinSquare1 = false;
//    mouseinSquare3 = false;
//    mouseinSquare4 = false;
//    mouseinSquare5 = false;
//}
//else {
//    mouseinSquare2 = false;
//}
//  if  (mouseX > square3X && mouseX < square3X + square2Width && mouseY > square3Y && mouseY < square3Y + square3Height) {
//    mouseinSquare3 = true;
//    mouseinSquare1 = false;
//    mouseinSquare2 = false;
//    mouseinSquare4 = false;
//    mouseinSquare5 = false; }
//    else {
//    mouseinSquare3 = false;
//}
//  if  (mouseX > square4X && mouseX < square4X + square4Width && mouseY > square4Y && mouseY < square4Y + square4Height) {
//    mouseinSquare4 = true;
//    mouseinSquare1 = false;
//    mouseinSquare2 = false;
//    mouseinSquare3 = false;
//    mouseinSquare5 = false; }
//    else {
//    mouseinSquare4 = false;
//}
//  if  (mouseX > square5X && mouseX < square5X + square5Width && mouseY > square5Y && mouseY < square5Y + square5Height) {
//    mouseinSquare5 = true;
//    mouseinSquare1 = false;
//    mouseinSquare2 = false;
//    mouseinSquare3 = false;
//    mouseinSquare4 = false; }
//    else {
//    mouseinSquare5 = false;
//}
    
//}
//}
//// //snaps the cubes to the grid
//void mouseReleased() {
//    square1X-=square1X%24;
//    square1Y-=square1Y%24;
    
//    square2X-=square2X%24;
//    square2Y-=square2Y%24;
    
//    square3X-=square3X%24;
//    square3Y-=square3Y%24;
    
//    square4X-=square4X%24;
//    square4Y-=square4Y%24;
    
//}
 



////creates a variable for the new square position
//float deltaX;
//float deltaY;
//float deltaX2;
//float deltaY2;
//float deltaX3;
//float deltaY3;
//float deltaX4;
//float deltaY4;
//float deltaX5;
//float deltaY5;

//// if the mouse is in the square and pressed, then move it when the mouse is dragged
//public void mouseDragged() {
//  if (mouseinSquare1) {
//    float deltaX = mouseX - pmouseX;
//    float deltaY = mouseY - pmouseY;
//     square1X += deltaX;
//     square1Y += deltaY;
//  }
//  if (mouseinSquare2) {  
//    float delta2X = mouseX - pmouseX;
//    float delta2Y = mouseY - pmouseY;
//    square2X += delta2X;
//    square2Y += delta2Y;
//  }
//  if (mouseinSquare3) {  
//    float delta3X = mouseX - pmouseX;
//    float delta3Y = mouseY - pmouseY;
//    square3X += delta3X;
//    square3Y += delta3Y;
//  }
//  if (mouseinSquare4) {  
//    float delta4X = mouseX - pmouseX;
//    float delta4Y = mouseY - pmouseY;
//    square4X += delta4X;
//    square4Y += delta4Y;
//  }
//  if (mouseinSquare5) {  
//    float delta5X = mouseX - pmouseX;
//    float delta5Y = mouseY - pmouseY;
//    square5X += delta5X;
//    square5Y += delta5Y;
//  }
//}