package heronarts.lx.data;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ProjectDataSource {
    /**
     * Provide a human-readable description of this data source
     *
     * @return a human-readable description of the data source
     */
    String sourceDescription();

    /**
     * Get a stream for reading the given file ID.
     *
     * @param fileType the type of the file being read
     * @param id the ID of the file being read (for file types where there's only one per project, this is null)
     * @return an open input stream for the data, or null if the file doesn't exist or can't be opened
     * @throws IOException when opening the stream fails
     */
    InputStream inputStream(ProjectFileType fileType, String id) throws IOException;
}
