/* autogenerated by Processing revision 1293 on 2024-01-12 */
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
WOOHOOOO THE GLOBE IS FULLY WORKING
NEXT STEPS:
    combine this and sphereTypes to make it more efficient
    make a gui (ask farrar how to approach it)
    calculate the rain and temp stuff in generateSphere in sphere class
    how the hell do i map the temps of mars? (no way im doing it manually). Maybe the same way im gonna do biomes? (with triangle mapping?)

POSSIBLE OPTIMIZATION:
maybe import the graph and add triangles to match it, then send out ray to check if it hits


ADDITIONS:
calculating total amt of water on the planet at a certain water level(gallons)

ecology stuff:
gulfstreams and grasslands and beaches
adding something to auto detect range of colors from low to high (so i dont have to use grayscale)
CLIMATE:
https://www.jstor.org/stable/24975952?seq=5 go page 5 for graph of temps on mars
*/
public static class Color {
    private double r,g,b;
   
    //just making colors to make life easier
    public static Color Tundra(){
        return new Color(148,169,174);
    }
    public static Color Grassland(){
        return new Color(147,127,44);
    }
    public static Color Woodland(){
        return new Color(180,125,1);
    }
    public static Color Boreal_Forest(){
        return new Color(91,144,81);
    }
    public static Color Seasonal_Forest(){
        return new Color(40,138,161);
    }
    public static Color Temperate_Forest(){
        return new Color(3,83,109);
    }
    public static Color Subtropical_Desert(){
        return new Color(201,114,52);
    }
    public static Color Savanna(){
        return new Color(152,167,34);
    }
    public static Color Tropical_Rainforest(){
        return new Color(1,82,44);
    }
    public Color(double r, double g, double b){
        this.r = r;
        this.g = g;
        this.b = b;
    }
    //returning the r,g,b of the color
    public double getR(){
        return r;
    }
    public double getG(){
        return g;
    }
    public double getB(){
        return b;
    }
    //adding two color's r g b values
    public Color add(Color C){
        return new Color(this.r + C.getR(), this.g + C.getG(), this.b+C.getB());
    }
    //multiply the r g b values of this color by a scalar
    public Color scale(double scalar){
        return new Color(this.r*scalar,this.g*scalar,this.b*scalar);
    }
    //whatever this does
    public int toARGB(){
        int ir = (int)(Math.min(Math.max(r,0),1) * 255 + 0.1f);
        int ig = (int)(Math.min(Math.max(g,0),1) * 255 + 0.1f);
        int ib = (int)(Math.min(Math.max(b,0),1) * 255 + 0.1f);
        return (ir << 16) | (ig << 8) | (ib << 0);
        //bit shifting 
    }
    //multiplies the color by the shading color c
    public Color shade(Color c){
        return new Color(this.getR()*c.getR(), this.getG()*c.getG(), this.getB()*c.getB());
    }
    //tints the color by the tinting color c
    public Color tint(Color c){
        double newR = r + (1 - r)*c.getR();
        double newG = g + (1 - g)*c.getG();
        double newB = b + (1 - b)*c.getB();
        return new Color(newR, newG, newB);

    }

}
class Controller{
public static final int DETAIL = 15;
public static final int RADIUS = 100;
public static final int SPHERE_MODE = 0;
public static final int ICO_RECURSIVE = 0;
public static final int WATER_LEVEL = 0;
public static final float ALTITUDE_SCALAR = 0.04f;
public static final String GREYSCALE_IMAGE = "marsTopography.jpeg";
}

PeasyCam cam;
Vector3D[][] globe;
PImage topography;
int w,h;
Sphere sphere;
String photo = Controller.GREYSCALE_IMAGE;
int waterLevel = Controller.WATER_LEVEL;
float altScalar = Controller.ALTITUDE_SCALAR;
int detail = Controller.DETAIL;
int radius = Controller.RADIUS;
int sphereMode = Controller.SPHERE_MODE;
int icoRecursive = Controller.ICO_RECURSIVE;
NormalizedCube[] cubeFaces = new NormalizedCube[6];
SpherifiedCube[] sCubeFaces = new SpherifiedCube[6];
Icosahedron ico = new Icosahedron(icoRecursive,radius);
Vector3D[] direction = {new Vector3D(0,-1,0), new Vector3D(0,1,0),new Vector3D(1,0,0),new Vector3D(-1,0,0),new Vector3D(0,0,1),new Vector3D(0,0,-1)};
String currentShape = "standard";
public void setup() {
    /* size commented out by preprocessor */;
    cam = new PeasyCam(this,500);
    topography = loadImage(photo);
    topography.resize(16,9);
    topography.loadPixels();
    sphere = new Sphere(topography.width, topography.height,100,globe);
    sphere.startSphere(currentShape);
    sphere.calculateBiomes();
    ico.createMesh();
    for(int i = 0; i < 6; i++){
        sCubeFaces[i] = new SpherifiedCube(detail,direction[i],radius);
        sCubeFaces[i].constructCube();
        cubeFaces[i] = new NormalizedCube(detail,direction[i],radius);
        cubeFaces[i].constructCube();
    }
    
}
public void draw() {
    background(0);
    fill(255);
    lights();
    noStroke();
    switch(sphereMode){
        case 0: 
            sphere.drawSphere();
            currentShape = "standard";
        break;
        case 1:
        for(int i = 0; i < 6; i++){
            cubeFaces[i].drawCube();
        }
        currentShape = "Normalized Cube";
        break;
        case 2:
        for(int i = 0; i < 6; i++){
            sCubeFaces[i].drawCube();
        }
        currentShape = "Spherified Cube";
        break;
        case 3:
        ico.draw();
        currentShape = "Icosahedron";
        break;
    } 
    if (keyPressed) {
        if (key == CODED) {
            if (keyCode == RIGHT) {
                waterLevel++; 
            }
            if (keyCode == LEFT) {
                waterLevel--; 
                sphere.scaleWaterDown();
            }
            if (keyCode == UP) {
                altScalar +=.01f; 
                //this is inneficient but idc
                sphere.regenSphere("standard");
            }
            if (keyCode == DOWN && altScalar > 0.01f) {
                altScalar -=.01f;
                //this is inneficient but idc
                sphere.regenSphere("standard");
            }
        }
        if(key == '1'){
            sphereMode = 0;
        }
        if(key == '2'){
            sphereMode = 1;
        }
        if(key == '3'){
            sphereMode = 2;
        }
        if(key == '4'){
            sphereMode = 3;
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
class Icosahedron{
    int resolution;
    Vector3D localUp;
    Vector3D axisA;
    Vector3D axisB;
    HashMap<Integer,Vector3D> verticesDict;
    int[] triangleArray;
    int radius;
    int recursionAmt;
    ArrayList<TriangleIndices> faces;
    ArrayList<TriangleIndices> faces2;
    double t = (1.0f + Math.sqrt(5.0f))/2.0f;
    public Icosahedron(int recursionAmt, int radius){
        this.recursionAmt = recursionAmt;
        this.radius = radius;
    }
    public void createMesh(){
        //setting 12 verticies
        verticesDict = new HashMap<Integer,Vector3D>();
        verticesDict.put(0, new Vector3D(-1,  t,  0));
        verticesDict.put(1, new Vector3D( 1,  t,  0));
        verticesDict.put(2, new Vector3D(-1, -t,  0));
        verticesDict.put(3, new Vector3D( 1, -t,  0));

        verticesDict.put(4, new Vector3D( 0, -1,  t));
        verticesDict.put(5, new Vector3D( 0,  1,  t));
        verticesDict.put(6, new Vector3D( 0, -1, -t));
        verticesDict.put(7, new Vector3D( 0,  1, -t));

        verticesDict.put(8, new Vector3D( t,  0, -1));
        verticesDict.put(9, new Vector3D( t,  0,  1));
        verticesDict.put(10,new Vector3D(-t,  0, -1));
        verticesDict.put(11,new Vector3D(-t,  0,  1));

        //setting the 20 faces
        faces = new ArrayList<TriangleIndices>();
        //first 5 surrounding p1
        faces.add(new TriangleIndices(0, 11, 5));
        faces.add(new TriangleIndices(0, 5, 1));
        faces.add(new TriangleIndices(0, 1, 7));
        faces.add(new TriangleIndices(0, 7, 10));
        faces.add(new TriangleIndices(0, 10, 11));

        // 5 adjacent faces
        faces.add(new TriangleIndices(1, 5, 9));
        faces.add(new TriangleIndices(5, 11, 4));
        faces.add(new TriangleIndices(11, 10, 2));
        faces.add(new TriangleIndices(10, 7, 6));
        faces.add(new TriangleIndices(7, 1, 8));

        // 5 faces around point 3
        faces.add(new TriangleIndices(3, 9, 4));
        faces.add(new TriangleIndices(3, 4, 2));
        faces.add(new TriangleIndices(3, 2, 6));
        faces.add(new TriangleIndices(3, 6, 8));
        faces.add(new TriangleIndices(3, 8, 9));

        // 5 adjacent faces
        faces.add(new TriangleIndices(4, 9, 5));
        faces.add(new TriangleIndices(2, 4, 11));
        faces.add(new TriangleIndices(6, 2, 10));
        faces.add(new TriangleIndices(8, 6, 7));
        faces.add(new TriangleIndices(9, 8, 1));

        //bisecting triangles
        for(int i = 0; i < recursionAmt; i++){
            faces2 = new ArrayList<TriangleIndices>();
            for(int j = 0; j < faces.size(); j++){
                //replace the triangles for 4
                //get the middle points of each triangle (a,b,c)
                TriangleIndices tri = faces.get(j);
                int a = faces.size() + (j*3);
                int b = faces.size() + (j*3) + 1;
                int c = faces.size() + (j*3) + 2;
                //3 vectors that get midpoints of the three vectors 
                Vector3D newA = verticesDict.get((tri.getV1())).add(verticesDict.get(tri.getV2())).divide(2);
                Vector3D newB = verticesDict.get((tri.getV2())).add(verticesDict.get(tri.getV3())).divide(2);
                Vector3D newC = verticesDict.get((tri.getV3())).add(verticesDict.get(tri.getV1())).divide(2);
                faces2.add(new TriangleIndices(tri.getV1(),a,c));
                faces2.add(new TriangleIndices(tri.getV2(),b,a));
                faces2.add(new TriangleIndices(tri.getV3(),c,b));
                faces2.add(new TriangleIndices(a,b,c));
                verticesDict.put(a,newA);
                verticesDict.put(b,newB);
                verticesDict.put(c,newC);
            }
            faces = faces2;
        }
    }
    public void draw(){
        for(int i = 0; i < faces.size(); i++){
            beginShape(TRIANGLES);
            //this SHOULD iterate thru each item in faces and therefore get the triangles and then draw them

            Vector3D p1 = verticesDict.get(faces.get(i).getV1()).normalize().scale(radius);
            Vector3D p2 = verticesDict.get(faces.get(i).getV2()).normalize().scale(radius);
            Vector3D p3 = verticesDict.get(faces.get(i).getV3()).normalize().scale(radius);
            vertex((float)p1.x,(float)p1.y,(float)p1.z);
            vertex((float)p2.x,(float)p2.y,(float)p2.z);
            vertex((float)p3.x,(float)p3.y,(float)p3.z);
            
            endShape();
        }
    }
}
/* Figure out the math for the amt of triangles every time the triangle is broken into 3 (3,6,15,45)

TRIANGLE STUFF:
the formula of total vertices is (n^2 +2)/2 where n is the amt of vertices on the bottom (two vertice) side
how to find amt of triangles on bottom? pattern: 2,3,5,9, (if u subtract initial val 2 it becomes 0,1,3,7 find an equation that follows that)]
amt of vertices on bottom = 2*recursion -1
amt of total vertices = ((bottom*bottom) + bottom) /2

for get middle point:
you need to find the mid point, add it to the faces2 array (being done) AND add it into the vertice array so it knows the vector of it

possible changes:
make verticesArray into a dict and map the points to a vector
*/
class NormalizedCube{
    int resolution;
    Vector3D localUp;
    Vector3D axisA;
    Vector3D axisB;
    Vector3D[] verticesArray;
    int[] triangleArray;
    int radius;
    public NormalizedCube(int resolution, Vector3D localUp, int radius){
        this.resolution = resolution;
        this.localUp = localUp;
        axisA = new Vector3D(localUp.y, localUp.z, localUp.x);
        axisB = localUp.cross(axisA);
        this.radius = radius;
    }
    public void constructCube(){
        verticesArray = new Vector3D[resolution*resolution];
        triangleArray = new int[((resolution-1)*(resolution-1))*6];
        int triIndex = 0;
        for(double y = 0; y < resolution; y++){
            for(double x = 0; x < resolution; x++){
                int i = (int) (x+(y*resolution));
                Vector2D percentDone = new Vector2D(x/(resolution-1),y/(resolution-1));
                Vector3D pointOnUnitCube = localUp.add(axisA.scale(((percentDone.x -.5f)*2))).add(axisB.scale(((percentDone.y -.5f)*2)));
                //println(pointOnUnitCube.x + " " + pointOnUnitCube.y + " " + pointOnUnitCube.z);
                //println(percentDone);
                verticesArray[i] = pointOnUnitCube;

                if(x != resolution-1  && y != resolution-1){
                    triangleArray[triIndex] = i;
                    triangleArray[triIndex+1] = i+resolution+1;
                    triangleArray[triIndex+2] = i+resolution;
                    triangleArray[triIndex+3] = i;
                    triangleArray[triIndex+4] = i+1;
                    triangleArray[triIndex+5] = i+resolution+1;
                    triIndex+= 6;
                }
            }
        }
    }
    public void drawCube(){
        for(int i = 0; i < triangleArray.length; i+=3){
            beginShape(TRIANGLES);
            Vector3D p1 = (verticesArray[triangleArray[i]]).normalize().scale(radius);
            Vector3D p2 = (verticesArray[triangleArray[i+1]]).normalize().scale(radius);
            Vector3D p3 = (verticesArray[triangleArray[i+2]]).normalize().scale(radius);
            vertex((float)p1.x,(float)p1.y,(float)p1.z);
            vertex((float)p2.x,(float)p2.y,(float)p2.z);
            vertex((float)p3.x,(float)p3.y,(float)p3.z);
            //println(p1.y);
            
            endShape();
        }
    }
}

/*
p1 x=0 , y= 1, z = -20
p2 x=0 , y= 1, z = -20

PROBLEMS:
percentDone rounds to int so thats fs a problem
*/
class Sphere {
    float x, y, z, r;
    int w, h;
    Vector3D[][] globe;
    Color[][] greyScale;
    double[][] altitude;
    float[][] tempMap;
    float[][] rainMap;
    Sphere(int w, int h, float r, Vector3D[][] globe) {
        this.w = w;
        this.h = h;
        this.r = r;
        this.globe = globe;
    }
    public void startSphere(String sphereType) {
        if(sphereType.toLowerCase().equals("standard")){
            greyScale = new Color[h][w];
            globe = new Vector3D[h][w];
            tempMap = new float[h][w];
            rainMap = new float[h][w];
            altitude = new double[h][w];
            x = 0;
            y = 0;
            z = 0;
            for (int i = 0; i < h; i++) {
                // mapping the latitude as i percent of the way to total (i/total) and putting
                // it into pi (if i is 25 then it would b 25 percent(25/100) of pi (.25/pi)
                float lat = map(i, 0, h, 0, PI);
                for (int j = 0; j < w; j++) {
                    // mapping the longitude as j percent of the way to total (j/total) and putting
                    // it into 2pi (j is 25 then it would b 25 percent of pi (.25/2pi)
                    float lon = map(j, 0, w, 0, 2 * PI);
                    // literally just polar coords
                    x = r * sin(lat) * sin(lon);
                    y = -r * cos(lat);
                    z = r * sin(lat) * cos(lon);
                    // storing the coords into array of vectors
                    globe[i][j] = new Vector3D(x, y, z);
                    int greyVal = Integer.parseInt(binary(topography.pixels[(i * w) + j] % 256, 8));
                    //saves it into an array ONCE
                    greyScale[i][j] = new Color(this.binConvert(greyVal),this.binConvert(greyVal),this.binConvert(greyVal));
                    altitude[i][j] =  greyScale[i][j].getR();
                    globe[i][j] = globe[i][j].scale((r + (altitude[i][j] * altScalar)) / r);
                    tempMap[i][j] = random(-10,35);
                    rainMap[i][j] = random(0,450);
                }
            }
        }        
    }
    public void drawSphere() {
        for(int i = 0; i < h; i++){
            for (int j = 0; j < w; j++) {
                if (altitude[i][j] <= waterLevel) {
                    globe[i][j] =globe[i][j].normalize().scale((r + ((waterLevel) * altScalar)));
                }
            }
        }
        for (int i = 0; i < h; i++) {
            beginShape(QUAD);
            for (int j = 0; j < w; j++) {
                // this is altitude stuff
                if (altitude[i][j] <= waterLevel) {
                    fill(0, 0, 255);
                } else {
                    fill((float)greyScale[i][j].getR(), (float)greyScale[i][j].getG(), (float)greyScale[i][j].getB());
                }
                if(i != h - 1 && j != w-1){ 
                    Vector3D v1 = globe[i][j];
                    Vector3D v2 = globe[i][j+1];
                    Vector3D v3 = globe[i+1][j]; 
                    Vector3D v4 = globe[i+1][j+1];
                    vertex((float)v1.x,(float) v1.y,(float) v1.z);
                    vertex((float)v2.x,(float) v2.y, (float)v2.z);
                    vertex((float)v4.x,(float) v4.y, (float)v4.z);
                    vertex((float)v3.x,(float) v3.y, (float)v3.z);
                }
                if(i == h-1 && j != w-1){
                    Vector3D v5 = globe[i][j];
                    Vector3D v6 = globe[i][j+1];
                    vertex((float)v5.x,(float) v5.y,(float) v5.z);
                    vertex((float)v6.x,(float) v6.y,(float) v6.z);
                    vertex((float)0,(float)(1 * ((r + (altitude[i][0] * altScalar)))),(float)0);
                    vertex((float)0,(float)(1 * ((r + (altitude[i][0] * altScalar)))),(float)0);
                }
            }
            //this is checking for last strip from (i=h) to 0
            Vector3D v7 = globe[i][w-1];
            Vector3D v8 = globe[i][0];
            vertex((float)v7.x,(float)v7.y,(float)v7.z);
            vertex((float)v8.x,(float)v8.y,(float)v8.z);
            if (i != h - 1) {
                Vector3D v9 = globe[i + 1][w-1];
                Vector3D v10 = globe[i + 1][0];
                vertex((float)v10.x,(float)v10.y,(float)v10.z);
                vertex((float)v9.x,(float)v9.y,(float)v9.z);
            } else {
                vertex((float)0,(float)(1 * ((r + (altitude[i][0] * altScalar)))),(float)0);
                vertex((float)0,(float)(1 * ((r + (altitude[i][0] * altScalar)))),(float)0);
            }
            
            endShape();
        }
    }
    public void regenSphere(String sphereType) {
        //drawing simple sphere
        if(sphereType.equals("standard")){
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
                    globe[i][j] = new Vector3D(x, y, z);
                    globe[i][j] = globe[i][j].scale((r + (altitude[i][j] * altScalar)) / r);
                    //altitude[i][j] = altitude[i][j] *((r + (altitude[i][j] * altScalar)) / r);
                    tempMap[i][j] = random(-10,35);
                    rainMap[i][j] = random(0,450);
                }
            }
        }        
    }
    public void scaleWaterDown() {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++){
                if (waterLevel <= altitude[i][j]) {
                   globe[i][j] = globe[i][j].normalize().scale((r + ((altitude[i][j]) * altScalar)));
                } else {
                    globe[i][j] = globe[i][j].normalize().scale((r + ((waterLevel) * altScalar)));
                }
            }
        }
    }
    public void calculateBiomes(){
        for(int i = 0; i < greyScale.length;i++){
            for(int j = 0; j < greyScale[i].length;j++){
                float temp = tempMap[i][j];
                float rain = rainMap[i][j];
                //bunch of if statements checking what biome it is based off x(temp) and y(rainfall)
                //is it better to do nested for loops here?
                if(temp < 0){
                    if(rain<100){
                       greyScale[i][j] = Color.Tundra();
                    }
                }
                if(0 < temp && temp < 7){
                    if(rain < 20){
                        greyScale[i][j] = Color.Grassland();
                    }
                    if(20 < rain && rain < 30){
                        greyScale[i][j] = Color.Woodland();
                    }
                    if(30 < rain){
                        greyScale[i][j] = Color.Boreal_Forest();
                    }
                }
                if(7<temp && temp <21){
                    if(rain < 25){
                        greyScale[i][j] = Color.Grassland();
                    }
                    if(25<rain && rain <100){
                         greyScale[i][j] = Color.Woodland();
                    }
                    if(100<rain && rain<200){
                        greyScale[i][j] = Color.Seasonal_Forest();
                    }
                    if(200<rain){
                        greyScale[i][j] = Color.Temperate_Forest();
                    }
                }
                if(21<temp){
                    if(rain<60){
                        greyScale[i][j] = Color.Subtropical_Desert();
                    }
                    if(60<rain && rain<250){
                        greyScale[i][j] = Color.Savanna();
                    }
                    if(250<rain){
                        greyScale[i][j] = Color.Tropical_Rainforest();
                    }
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
/* NOTES:
altitude is now an array which holds the values of each pixels altitude (called once and is unchanged)
*/
class SpherifiedCube{
    int resolution;
    Vector3D localUp;
    Vector3D axisA;
    Vector3D axisB;
    Vector3D[] verticesArray;
    int[] triangleArray;
    int radius;
    public SpherifiedCube(int resolution, Vector3D localUp, int radius){
        this.resolution = resolution;
        this.localUp = localUp;
        axisA = new Vector3D(localUp.y, localUp.z, localUp.x);
        axisB = localUp.cross(axisA);
        this.radius = radius;
    }
    public void constructCube(){
        verticesArray = new Vector3D[resolution*resolution];
        triangleArray = new int[((resolution-1)*(resolution-1))*6];
        int triIndex = 0;
        for(double y = 0; y < resolution; y++){
            for(double x = 0; x < resolution; x++){
                int i = (int) (x+(y*resolution));
                Vector2D percentDone = new Vector2D(x/(resolution-1),y/(resolution-1));
                Vector3D p = localUp.add(axisA.scale(((percentDone.x -.5f)*2))).add(axisB.scale(((percentDone.y -.5f)*2)));
                double x2 = p.x * p.x;
                double y2 = p.y * p.y;
                double z2 = p.z * p.z;
                Vector3D newPoint = new Vector3D(0,0,0);
                newPoint.x = p.x * Math.sqrt(1 - y2 / 2 - z2 / 2 + y2 * z2 / 3);
		        newPoint.y = p.y * Math.sqrt(1 - x2 / 2 - z2 / 2 + x2 * z2 / 3);
		        newPoint.z = p.z * Math.sqrt(1 - x2 / 2 - y2 / 2 + x2 * y2 / 3);
                verticesArray[i] = newPoint;

                if(x != resolution-1  && y != resolution-1){
                    triangleArray[triIndex] = i;
                    triangleArray[triIndex+1] = i+resolution+1;
                    triangleArray[triIndex+2] = i+resolution;
                    triangleArray[triIndex+3] = i;
                    triangleArray[triIndex+4] = i+1;
                    triangleArray[triIndex+5] = i+resolution+1;
                    triIndex+= 6;
                }
            }
        }
    }
    public void drawCube(){
        for(int i = 0; i < triangleArray.length; i+=3){
            beginShape(TRIANGLES);
            Vector3D p1 = (verticesArray[triangleArray[i]]).normalize().scale(radius);
            Vector3D p2 = (verticesArray[triangleArray[i+1]]).normalize().scale(radius);
            Vector3D p3 = (verticesArray[triangleArray[i+2]]).normalize().scale(radius);
            vertex((float)p1.x,(float)p1.y,(float)p1.z);
            vertex((float)p2.x,(float)p2.y,(float)p2.z);
            vertex((float)p3.x,(float)p3.y,(float)p3.z);
            //println(p1.y);
            
            endShape();
        }
    }
}

/*
p1 x=0 , y= 1, z = -20
p2 x=0 , y= 1, z = -20

PROBLEMS:
percentDone rounds to int so thats fs a problem
*/
class TriangleIndices{
    public int v1,v2,v3;
    public TriangleIndices(int v1, int v2, int v3){
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }
    public int getV1(){
        return v1;
    }
    public int getV2(){
        return v2;
    }
    public int getV3(){
        return v3;
    }
}
class Vector2D{
  public double x;
  public double y;

  public Vector2D(double x, double y){
    this.x = x;
    this.y = y;
    
  }
  public double getX(){
    return x;
  }
  public double getY(){
    return y;
  }
  public void applyForce(Vector2D force){
    x = x + force.getX();
    y = y + force.getY();
  }
  public Vector2D scale(double scalar){
    return new Vector2D(x*scalar,y*scalar);
  }
  public Vector2D add(Vector2D v){
    return new Vector2D(x+v.getX(),y+v.getY());
  }
  public Vector2D subtract(Vector2D v){
    return new Vector2D(x-v.getX(),y-v.getY());
  }
  public double dot(Vector2D v){
    return (x*v.getX() + y*v.getY());
  }
  public double length(){
    return Math.sqrt(this.dot(this));
  }
  public Vector2D normalize(){
    return this.scale(1/this.length());
  }
   public String toString(){
    return "(" + x + ", " + y + ")";
  }
}
class Vector3D{
  public double x;
  public double y;
  public double z;
  public Vector3D(double x, double y, double z){
    this.x = x;
    this.y = y;
    this.z = z;
  }
  public double getx(){
    return x;
  }
  public double gety(){
    return y;
  }
  public double getz(){
    return z;
  }
  public void applyForce(Vector3D force){
    x = x + force.getx();
    y = y + force.gety();
    z = z + force.getz();
  }
  public Vector3D scale(double scalar){
    return new Vector3D(x*scalar,y*scalar,z*scalar);
  }
  public Vector3D divide(double scalar){
    return new Vector3D(x/scalar,y/scalar,z/scalar);
  }
  public Vector3D add(Vector3D v){
    return new Vector3D(x+v.getx(),y+v.gety(),z+v.getz());
  }
  public Vector3D subtract(Vector3D v){
    return new Vector3D(x-v.getx(),y-v.gety(),z-v.getz());
  }
  public double dot(Vector3D v){
    return (x*v.getx() + y*v.gety() + z*v.getz());
  }
  public double length(){
    return Math.sqrt(this.dot(this));
  }
  public Vector3D normalize(){
    return this.scale(1/this.length());
  }
  public Vector3D cross(Vector3D v){
    return new Vector3D((this.y*v.z) - (this.z*v.y),(this.z*v.x)- (this.x*v.z), (this.x*v.y)-(this.y*v.x));
  }

  public String toString(){
    return "(" + x + ", " + y + ", " + z + ")";
  }
  public  Vector3D Up(){ return new Vector3D(0,1,0);}
  public  Vector3D Down(){ return new Vector3D(0,-1,0);}
  public  Vector3D Left(){ return new Vector3D(-1,0,0);}
  public  Vector3D Right(){ return new Vector3D(1,0,0);}
  public  Vector3D Forward(){ return new Vector3D(0,0,-1);}
  public  Vector3D Backward(){ return new Vector3D(0,0,1);}
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
