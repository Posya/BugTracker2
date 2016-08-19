package site.kiselev.datastore;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Tests for SimpleFileDatastore
 */
@SuppressWarnings("FieldCanBeLocal")
public class SimpleFileDatastoreTest {
    private final String BASE_PATH = "./testdata/db";
    private final String FILE_SUFFIX = ".json";

    @Test
    public void setAndGet() throws Exception {
        Random r = new Random(System.currentTimeMillis());
        String testString = Integer.toString(r.nextInt(100000));

        Datastore ds = new SimpleFileDatastore(BASE_PATH, FILE_SUFFIX);

        ds.set(new String[]{"posya", "tasks"}, "testdata" + testString);
        String s = ds.get(new String[]{"posya", "tasks"});
        assertEquals("testdata" + testString, s);
    }

}