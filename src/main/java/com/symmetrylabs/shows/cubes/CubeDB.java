package com.symmetrylabs.shows.cubes;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dongliu.gson.GsonJava8TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;


public class CubeDB {
    private static final String DATABASE_DIR = "shows/cubes/db";

    public final List<CubeRecord> records;

    protected CubeDB(List<CubeRecord> records) {
        this.records = records;
    }

    public static CubeDB load() throws IOException {
        List<CubeRecord> records = new ArrayList<>();
        Path dir = new File(DATABASE_DIR).toPath();
        final Gson loader = new GsonBuilder().registerTypeAdapterFactory(new GsonJava8TypeAdapterFactory()).create();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (path.toString().endsWith(".cube.json")) {
                    JsonReader reader = new JsonReader(new FileReader(path.toFile()));
                    records.add(loader.fromJson(reader, CubeRecord.class));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return new CubeDB(records);
    }
}
