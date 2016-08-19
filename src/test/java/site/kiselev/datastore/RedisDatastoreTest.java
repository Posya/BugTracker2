package site.kiselev.datastore;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Tests for Redis Datastore
 */
public class RedisDatastoreTest {

    private RedisDatastore rd = new RedisDatastore();


    @Test
    public void get() throws Exception {
        String s = rd.get(new String[]{"test"});
        assertEquals("testvalue", s);
    }

    @Test
    public void set() throws Exception {
        Random r = new Random(System.currentTimeMillis());
        String testString = Integer.toString(r.nextInt(100000));

        rd.set(new String[]{"test", "random"}, "testdata" + testString);
        String s = rd.get(new String[]{"test", "random"});
        assertEquals("testdata" + testString, s);
    }

    @Test
    public void getWrongKey() {
        String s = rd.get(new String[]{"wrong", "key"});
        assertEquals(null, s);
    }

    @Test
    public void zAddAndRange() {
        Random r = new Random(System.currentTimeMillis());

        Map<Integer, String> rs = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            int e = r.nextInt(100000);
            rd.zAdd(new String[]{"test", "zadd"}, e, "testdata" + e);
            rs.put(e, "testdata" + e);
        }

        int score = r.nextInt(100000);

        Set<String> expected = rs.entrySet().stream()
                .filter(x -> x.getKey() <= score)
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        Set<String> actual = rd.zRangeLessThen(new String[]{"test", "zadd"}, score);
        assertEquals(expected, actual);

        for (String s : rs.values()) {
            rd.zRem(new String[]{"test", "zadd"}, s);
        }

        actual = rd.zRangeLessThen(new String[]{"test", "zadd"}, 100001);
        assertEquals(new HashSet<String>(), actual);

    }

}