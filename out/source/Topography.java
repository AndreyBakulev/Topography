/* autogenerated by Processing revision 1293 on 2023-12-11 */
import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import peasy.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Topography extends PApplet {

/* 
PROBLEMS:
keep optimizing
scaleWater is not correct

POSSIBLE OPTIMIZATION:
make a list of all pixels and remove the ones that are already below water level (same time complexity?)
the double for loops in drawSphere() that check if waterlevel is higher is O(n^2) i think...
if i make a 1d array and check it like that (similar to photo.pixels[]), will that be O(n)?

ADDITIONS:
make the ground colored (map it from greyval seems ez)
try using a fibonacci sphere (ask farrar)
make water 3d (simply multiply the vector by the waterLevel (this is weird cus it keeps multiplying it and then it stacks and isnt even))
make the water hold its color as it goes (rainbow like)
adding compass
calculating total amt of water on the planet at a certain water level(gallons)

ecology stuff:
gulfstreams and grasslands and beaches
adding something to auto detect range of colors from low to high (so i dont have to use grayscale)
CLIMATE:
https://www.jstor.org/stable/24975952?seq=5 go page 5 for graph of temps on mars
*/

PeasyCam cam;
PVector[][] globe;
PImage topography;
int w,h;
String photo = "marsTopography.jpeg";
Sphere sphere;
int waterLevel = 0;
float altScalar =.1f;
public void setup() {
    /* size commented out by preprocessor */;
    cam = new PeasyCam(this,500);
    topography = loadImage(photo);
    topography.resize(640,360);
    topography.loadPixels();
    sphere = new Sphere(0,0,0,topography.width, topography.height,100,globe);
    sphere.generateSphere();
}
public void draw() {
    background(0);
    fill(255);
    lights();
    noStroke();
    sphere.drawSphere();
    if (keyPressed) {
        if (key == CODED) {
            //sphere.scaleWater();
            if (keyCode == RIGHT) {
                waterLevel++; 
            }
            if (keyCode == LEFT) {
                waterLevel--; 
            }
            if (keyCode == UP) {
                altScalar +=.01f; 
                //this is inneficient but idc
                sphere.generateSphere();
            }
            if (keyCode == DOWN) {
                altScalar -=.01f;
                //this is inneficient but idc
                sphere.generateSphere();
            }
        }
        
}
    
    textSize(50);
    fill(0,408,612);
    text("Water Level: " + waterLevel, - 150,200);
    text("Altitude Scalar: " + altScalar, - 150,250);
    //string stuff for fun ig :D
    String planetName = photo.substring(0,photo.indexOf("Topography"));
    //lol all of this long code just to capitalize
    text("Planet: " + planetName.substring(0,1).toUpperCase() + planetName.substring(1), - 125, - 200);
    
}




class Sphere {
    float x, y, z, r;
    int w, h;
    PVector[][] globe;
    int[][] greyScale;
    int groundLevel = 30;
    int altitude;
    
    Sphere(float x, float y, float z, int w, int h, float r, PVector[][] globe) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.h = h;
        this.r = r;
        this.globe = globe;
    }
    public void generateSphere() {
        greyScale = new int[h][w];
        globe = new PVector[h][w];
        for (int i = 0; i < h; i++) {
            // mapping the latitude as i percent of the way to total (i/total) and putting
            // it into pi (if i is 25 then it would b 25 percent(25/100) of pi (.25/pi)
            float lat = map(i, 0, h, 0, PI);
            for (int j = 0; j < w; j++) {
                // mapping the longitude as j percent of the way to total (j/total) and putting
                // it into 2pi (j is 25 then it would b 25 percent of pi (.25/2pi)
                float lon = map(j, 0, w, 0, 2 * PI);
                // literally just polar coords
                float x = r * sin(lat) * sin(lon);
                float y = -r * cos(lat);
                float z = r * sin(lat) * cos(lon);
                // storing the coords into array of vectors
                globe[i][j] = new PVector(x, y, z);
                int greyVal = Integer.parseInt(binary(topography.pixels[(i * w) + j] % 256, 8));
                //saves it into an array ONCE
                greyScale[i][j] = this.binConvert(greyVal);
                altitude = greyScale[i][j] - groundLevel;
                globe[i][j].mult((r + (altitude * altScalar)) / r);
            }
        }
        
    }
    public void drawSphere() {
        for (int i = 0; i < h; i++) {
            // quad prob easier but tri could b as well
            beginShape(QUAD_STRIP);
            for (int j = 0; j < w; j++) {
                if (greyScale[i][j] <= waterLevel) {
                    fill(waterLevel, waterLevel / 2, waterLevel * 2);
                } else {
                    fill(greyScale[i][j], greyScale[i][j], greyScale[i][j]);
                }
                // this is altitude stuff
                
                PVector v1 = globe[i][j];
                // top left point
                vertex(v1.x, v1.y, v1.z);
                //this is checking for south pole (i=h) to 0
                if (i != h - 1) {
                    PVector v2 = globe[i + 1][j];
                    vertex(v2.x, v2.y, v2.z);
                } else {
                    vertex(0,1 * r,0);
                }
                // bottom left point
                
            }
            //this is checking for last strip from (i=h) to 0
            PVector v3 = globe[i][0];
            vertex(v3.x,v3.y,v3.z);
            if (i != h - 1) {
                PVector v4 = globe[i + 1][0];
                vertex(v4.x,v4.y,v4.z);
            } else {
                vertex(0,1 * r,0);
            }
            
            endShape();
        }
        
    }
    public void scaleWater() {
        //this is def not correct or efficient
        //multiplying itself and not getting reset thats why its bigger than the ground 'above' it
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (greyScale[i][j] <= waterLevel) {
                    globe[i][j].mult((r + (waterLevel * .00f)) / r);
                }
            }
    }
    }
    
    public int binConvert(int binary) {
        int decimal = 0;
        int n = 0;
        while(true) {
            if (binary == 0) {
                break;
            } else {
                int temp = binary % 10;
                decimal += temp * Math.pow(2, n);
                binary /= 10;
                n++;
            }
        }
        return decimal;
    }
    
}


  public void settings() { size(1280, 720, P3D); }

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Topography" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
