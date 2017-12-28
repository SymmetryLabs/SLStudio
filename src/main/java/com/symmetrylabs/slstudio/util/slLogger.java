package com.symmetrylabs.slstudio.util;

import static com.symmetrylabs.slstudio.util.Utils.dataPath;
import static com.symmetrylabs.slstudio.util.Utils.println;



import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import oscP5.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;


public class slLogger {  //extends PApplet?

    OscP5 oscP5;
    PrintWriter out;
    String LOGFILE = "out.txt";

    public slLogger(){

            System.out.println("slLogger class init!");

        /**
         * Creates a new file including all subfolders
         */

        frameRate(25);
            /* start oscP5, listening for incoming messages at port 12000 */
            oscP5 = new OscP5(this, 3131);

            File f = new File(dataPath(LOGFILE));
            if (!f.exists()) {
                println(f);
                createFile(f);
            } else {
            }
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
            }
            catch (IOException e) {
                e.printStackTrace();
            }

    }


        // file this will log to. will always append!

        public void setup() {
            println("running setup");

            frameRate(25);
            /* start oscP5, listening for incoming messages at port 12000 */
            oscP5 = new OscP5(this, 3131);

            File f = new File(dataPath(LOGFILE));
            if (!f.exists()) {
                println(f);
                createFile(f);
            } else {
            }
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void draw() {
            background(0);
        }

        public void oscEvent(OscMessage m) {

            JSONObject log = new JSONObject();
            JSONArray data = new JSONArray();

            log.setString("route", m.addrPattern());
            log.setString("raw", new String(m.getBytes()));

            Date d = new Date();
            log.setLong("time", d.getTime());
            log.setJSONArray("data", data);

            String tag = m.typetag();
            for (int i = 0; i < tag.length(); i++) {
                char c = tag.charAt(i);
                OscArgument arg = m.get(i);
                switch(c) {
                    case 'i':
                        data.append(arg.intValue());
                        break;
                    case 'f':
                        data.append(arg.floatValue());
                        break;
                    case 's':
                        data.append(arg.stringValue());
                        break;
                    case 'b':
                        data.append(new String(arg.blobValue()));
                        break;
                    default:
                        println("UNKNOWN TYPE TAG");
                        exit();
                }
                //Process char
            }

            println("NEAT");

            //log.

            log.write(out, "compact");
            out.println("");

            //out.println(log.toString("compact"));
            out.flush();
        }

//    public void settings() {  size(400, 400); }
//    static public void main(String[] passedArgs) {
//        String[] appletArgs = new String[] { "logger" };
//        if (passedArgs != null) {
//            PApplet.main(concat(appletArgs, passedArgs));
//        } else {
//            PApplet.main(appletArgs);
//        }
//    }


}
