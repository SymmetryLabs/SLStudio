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
import heronarts.lx.LX;


public class Project {
    protected final Path root;
    protected final boolean forceLegacyProject;
    protected boolean isLegacyProject;
    protected final int runtimeVersion;

    protected final Map<ProjectFileType, List<Path>> projectFilesByType;

    public Project(Path root, int runtimeVersion) {
        this(root, runtimeVersion, false);
    }

    public Project(Path root, int runtimeVersion, boolean forceLegacy) {
        this.root = root;
        this.forceLegacyProject = forceLegacy;
        this.runtimeVersion = runtimeVersion;

        projectFilesByType = new HashMap<>();

        try {
            loadContents();
        } catch (IOException e) {
            System.err.println("couldn't load project root " + root + ":");
            e.printStackTrace();
        }
    }

    protected void loadContents() throws IOException {
        projectFilesByType.clear();

        /* Check for legacy projects */
        File df = root.toFile();
        if (forceLegacyProject || (df.isFile() && ProjectFileType.LegacyProjectFile.extension.equals(getExtension(root.toString())))) {
            addFile(root, ProjectFileType.LegacyProjectFile);
            isLegacyProject = true;
            return;
        }

        isLegacyProject = false;
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
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

    public int getRuntimeVersion() {
        return runtimeVersion;
    }

    public String getName() {
        return root.toFile().getName();
    }

    public Path getRoot() {
        return root;
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

    public void save(LX lx) {
        LegacyProjectLoader.save(this, lx);
    }

    public void load(LX lx) {
        LegacyProjectLoader.load(this, lx);
    }

    @Override
    public String toString() {
        return String.format("project @ %s (legacy=%s)", root, isLegacyProject);
    }

    private static String getExtension(String fname) {
        String[] extensionBits = fname.split(".");
        return extensionBits[extensionBits.length - 1];
    }

    public static Project createLegacyProject(File file, int runtimeVersion) {
        return new Project(file.toPath(), runtimeVersion, true);
    }
}
