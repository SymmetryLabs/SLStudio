package com.symmetrylabs.slstudio.model;

/*
A class which is equivalent to LXVector with one substantial difference:
DiscreteVector exists in discrete space (integer coordinates)
It may take on any arbitrary mapping to world space although a common one will be for coordinates of cube locations
 */
public class DiscreteVector {
    public int x;
    public int y;
    public int z;

    public DiscreteVector (int xx, int yy, int zz){
        x = xx;
        y = yy;
        z = zz;
    }
}
