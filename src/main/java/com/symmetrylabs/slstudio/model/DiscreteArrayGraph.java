package com.symmetrylabs.slstudio.model;

import com.jogamp.common.util.ArrayHashSet;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.ArrayList;
import java.util.List;

public class DiscreteArrayGraph {
    public static ArrayNode[][][] graph;

    /*
    This is a node that lives in the Array graph.  It contains pointers to adjacent objects (strips, other nodes)
     */
    private static class ArrayNode {
        // statically alocated the ordering is as follows:
        // 0: strip pointed upwards away from node
        // 1: strip pointed "north" from the node
        // 2: strip pointed "east" from the node
        // 3: strip pointed "south" from the node
        // 4: strip pointed "west" from the node
        // 5: strip pointed downward from the node
        Strip[] edges = new Strip[6];

        ArrayNode(Strip edge, int strip_location){
            assert strip_location < 6 && strip_location >=0 : "strip_location enum needs to be within bounds 0 and 5";
            edges[strip_location] = edge;
        }
    }

    public DiscreteArrayGraph(int extent_x, int extent_y, int extent_z){

        // instantiate the array
        graph = new ArrayNode[extent_x][extent_y][extent_z];

        // initialize to null (is this necessary?)
        for (int i = 0; i < extent_x; i++){
            for (int j = 0; j < extent_y; j++){
                for (int k = 0; k < extent_z; k++){
                    graph[i][j][k] = null;
                }
            }
        }

    }

//    public void addNode (DiscreteVector coords, Strip edge){
//        graph[coords.x][coords.y][coords.z] = new ArrayNode();
//    }

}
