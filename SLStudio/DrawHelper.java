import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple job holding class that allows patterns to queue rendering work for the main processing thread, which is
 * required for using OpenGL and such.
 *
 * Simply call DrawHelper.queueJob(id, someRunnable) to have that code executed on the main thread.
 *
 * The id should be a unique value per instance of the pattern or other component. Only the latest job added for an id
 * will be executed.
 */
public class DrawHelper {
    private static Map<String, Runnable> jobs = Collections.synchronizedMap(new HashMap<String, Runnable>());

    public static void queueJob(String id, Runnable job) {
        jobs.put(id, job);
    }

    public static void runAll() {
        List<Runnable> ourJobs;

        synchronized (jobs) {
            ourJobs = new ArrayList<Runnable>(jobs.values());
            jobs.clear();
        }

        for (Runnable job : ourJobs) {
            job.run();
        }
    }
}
