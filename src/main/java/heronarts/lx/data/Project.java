package heronarts.lx.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Project {
    protected final Path directory;
    protected final boolean forceLegacyProject;
    protected boolean isLegacyProject;

    protected final Map<ProjectFileType, List<Path>> projectFilesByType;

    public Project(Path directory) {
        this(directory, false);
    }

    public Project(Path directory, boolean forceLegacy) {
        this.directory = directory;
        this.forceLegacyProject = forceLegacy;

        projectFilesByType = new HashMap<>();

        try {
            loadContents();
        } catch (IOException e) {
            System.err.println("couldn't load project directory " + directory + ":");
            e.printStackTrace();
        }
    }

    protected void loadContents() throws IOException {
        projectFilesByType.clear();

        /* Check for legacy projects */
        File df = directory.toFile();
        if (forceLegacyProject || (df.isFile() && ProjectFileType.LegacyProjectFile.extension.equals(getExtension(directory.toString())))) {
            addFile(directory, ProjectFileType.LegacyProjectFile);
            isLegacyProject = true;
            return;
        }

        isLegacyProject = false;
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                ProjectFileType ft = ProjectFileType.fromExtension(getExtension(path.toString()));
                if (ft != null) {
                    addFile(path, ft);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    protected void addFile(Path path, ProjectFileType ft) {
        synchronized (projectFilesByType) {
            if (!projectFilesByType.containsKey(ft)) {
                projectFilesByType.put(ft, new ArrayList<>());
            }
            projectFilesByType.get(ft).add(path);
        }
    }

    public String getName() {
        return directory.toFile().getName();
    }

    public Path getRoot() {
        return directory;
    }

    public boolean isLegacyProject() {
        return isLegacyProject;
    }

    public List<Path> getAll(ProjectFileType ft) {
        if (projectFilesByType.containsKey(ft)) {
            return projectFilesByType.get(ft);
        }
        return new ArrayList<>();
    }

    private static String getExtension(String fname) {
        String[] extensionBits = fname.split(".");
        return extensionBits[extensionBits.length - 1];
    }

    public static Project createLegacyProject(File file) {
        return new Project(file.toPath(), true);
    }
}
