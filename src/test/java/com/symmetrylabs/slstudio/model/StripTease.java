package com.symmetrylabs.slstudio.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import heronarts.lx.model.LXPoint;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class StripTease {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
//        .excludeFieldsWithoutExposeAnnotation()
        .create();

    @Test
    public void tryWriteFile() throws IOException {
        StripForm strip = new StripForm("testStrip", new StripForm.Metrics(2, 2.5));
        JsonWriter writer = new JsonWriter(new FileWriter("data/test.json"));

        System.out.println(gson.toJson(strip));
    }
}
