package heronarts.lx.data;

import heronarts.lx.LX;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;

public interface ProjectDataSink {
    /**
     * Provide a human-readable description of this data sink
     *
     * @return a human-readable description of the data sink
     */
    String sinkDescription();

    /**
     * Get a stream for writing the given file ID.
     *
     * @param fileType the type of file being written
     * @param id the ID of the file being read (for file types where there's only one per project, this is null)
     * @return an open output stream for the file, or null if the file couldn't be opened
     * @throws IOException when opening the stream fails
     */
    @Nullable
    OutputStream outputStream(ProjectFileType fileType, String id) throws IOException;

    /**
     * Called when we begin writing data out for a project save.
     *
     * @param lx the LX instance whose state we're writing out
     * @param project the project we're starting a write for
     */
    void onWriteStart(LX lx, Project project);

    /**
     * Called when we're done writing data for the moment.
     *
     * This, in combination with onWriteStart, allows for a sort of transactionality,
     * where a number of writes to different kinds of project files can be batched.
     */
    void onWriteFinish() throws IOException;
}
