package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.shows.kalpa.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;

public class TreeGameOfLife extends TreePattern {
    public String getAuthor() {
        return "Wilco V.";
    }

    // This is a parameter, it has a label, an intial value and a range
    public final CompoundParameter t_step =
        new CompoundParameter("Step time", 10.0f, 1.0f, 10000.0)
            .setDescription("Controls the step time");

    public final DiscreteParameter life_drainage =
        new DiscreteParameter("Drainage", 3, 0, 4)
            .setDescription("Drainage per timestep");

    public final DiscreteParameter life_loneliness =
        new DiscreteParameter("Loneliness", 20, 0, 25)
            .setDescription("Penalty for loneliness per timestep");

    public final DiscreteParameter life_crowded =
        new DiscreteParameter("Overcrowded", 20, 0, 25)
            .setDescription("Penalty for overcrowding per timestep");

    public final DiscreteParameter life_boost =
        new DiscreteParameter("Boost", 10, 0, 25)
            .setDescription("Boost for ideal nr of neighbours per timestep");

    public final CompoundParameter spawn_percentage =
        new CompoundParameter("Spawn percentage", 1.0, 0.0, 1.0)
            .setDescription("Percentage of max health when spawning");

    public final DiscreteParameter max_life =
        new DiscreteParameter("Max health", 7000, 1, 10000)
            .setDescription("Maximum health");

    // Array of cells
    public final int[][][] world;
    public final int[][] world_indices;
    public double cur_step_time = 0.0;
    public int grid_size = 60;
    //public int max_life = 1000;
    //public float spawn_percentage = 0.25; // [0 - 1]

    //public int life_drainage     = 1;  // penalty
    //public int life_loneliness   = 9;  // penalty
    //public int life_crowded      = 9;  // penalty
    //public int life_boost        = 10; // boost

    public float color_h_life    = 80.0f;
    public float color_h_offset  = 0.2f;

    public float color_s_life    = 80.0f;

    public float color_b_life    = 50.0f;
    public float color_b_offset  = 15.0f;

    public int neighbours_min = 3;
    public int neighbours_max = 5;
    public int neighbours_boost = 4;

    public TreeGameOfLife(LX lx) {
        super(lx);
        addParameter(t_step);
        addParameter(spawn_percentage);
        addParameter(max_life);
        addParameter(life_drainage);
        addParameter(life_loneliness);
        addParameter(life_crowded);
        addParameter(life_boost);

        world = new int[grid_size][grid_size][grid_size];
        world_indices = new int[tree.leaves.size()][3];

        float xmin = 10000.0f, xmax = 0.0f, ymin = 10000.0f, ymax = 0.0f, zmin = 10000.0f, zmax = 0.0f;
        for (TreeModel.Leaf leaf : tree.leaves) {
            if(leaf.x < xmin){
                xmin = leaf.x;
            }else if(leaf.x > xmax){
                xmax = leaf.x;
            }
            if(leaf.y < ymin){
                ymin = leaf.y;
            }else if(leaf.y > ymax){
                ymax = leaf.y;
            }
            if(leaf.z < zmin){
                zmin = leaf.z;
            }else if(leaf.z > zmax){
                zmax = leaf.z;
            }
        }


        int l = 0;
        for (TreeModel.Leaf leaf : tree.leaves) {
            world_indices[l][0] = Math.round((leaf.x - xmin) / (xmax - xmin) * (grid_size-1));
            world_indices[l][1] = Math.round((leaf.y - ymin) / (ymax - ymin) * (grid_size-1));
            world_indices[l][2] = Math.round((leaf.z - zmin) / (zmax - zmin) * (grid_size-1));
            l++;
        }
        for(int x = 0; x < grid_size; x = x + 1){
            for(int y = 0; y < grid_size; y = y + 1){
                for(int z = 0; z < grid_size; z = z + 1){
                    float state = random(100);
                    if(state > 15){
                        world[x][y][z] = (int) (spawn_percentage.getValuef() * max_life.getValuei() * random(100) / 100.0f);
                    }
                }
            }
        }
    }

    public void update_world(double deltaMs) {
        boolean update_world_now = false;
        cur_step_time = cur_step_time + deltaMs;
        if(cur_step_time > this.t_step.getValuef()){
            cur_step_time = 0.0;
            update_world_now = true;
        }
        if(update_world_now){
            for(int x = 0; x < grid_size; x++){
                for(int y = 0; y < grid_size; y++){
                    for(int z = 0; z < grid_size; z++){
                        int number_of_neighbours = 0;
                        for(int xi = x-1; xi <= x+1; xi++){
                            for(int yi = y-1; yi <= y+1; yi++){
                                for(int zi = z-1; zi <= z+1; zi++){
                                    if(x!= xi && y!=yi && z!=zi && xi >= 0 && xi < grid_size && yi >= 0 && yi < grid_size && zi >= 0 && zi < grid_size){
                                        if(world[xi][yi][zi] > 0){
                                            number_of_neighbours++;
                                        }
                                    }
                                }
                            }
                        }
                        // Should we live or should we die?
                        if(world[x][y][z] > 0){
                            // We were alive
                            world[x][y][z] -= life_drainage.getValuei();
                            if(number_of_neighbours < neighbours_min){
                                world[x][y][z] -= life_loneliness.getValuei();
                            }else if(number_of_neighbours > neighbours_max){
                                world[x][y][z] -= life_crowded.getValuei();
                            }else if(number_of_neighbours == neighbours_boost && world[x][y][z] < max_life.getValuei()){
                                world[x][y][z] += life_boost.getValuei();
                            }else{
                                //world[x][y][z] += 1;
                            }
                        }else{
                            // We were dead
                            if(number_of_neighbours >= neighbours_min && number_of_neighbours <= neighbours_max){
                                // Enough neighbours, let's spawn
                                world[x][y][z] = Math.round(spawn_percentage.getValuef() * max_life.getValuei());
                            }
                        }
                    }
                }
            }
        }
}

    public void run(double deltaMs) {
        // Update the world
        update_world(deltaMs);
        // Let's iterate over all the leaves...
        int l = 0;
        for (TreeModel.Leaf leaf : tree.getLeaves()) {
            //print("leaf_ind: " + l + ",  x_ind: " + world_indices[l][0] + ",  y_ind: " + world_indices[l][1] + ",  z_ind: " + world_indices[l][2]);
            int leaf_life = world[world_indices[l][0]][world_indices[l][1]][world_indices[l][2]];
            if (leaf_life > 0) {
                setColor(leaf, LX.hsb(Math.round(Math.max(0.0f, Math.min(color_h_life, color_h_life * (leaf_life - color_h_offset * max_life.getValuei()) / ((1.0f - color_h_offset) * max_life.getValuei())))), Math.round(color_s_life) , Math.round(color_b_life * Math.sqrt(leaf_life) / Math.sqrt(max_life.getValuei())) + color_b_offset));
            } else {
                setColor(leaf, 0);
            }
            l++;
        }
    }
}
