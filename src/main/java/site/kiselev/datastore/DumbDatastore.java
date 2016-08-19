package site.kiselev.datastore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DumbDatastore
 */
public class DumbDatastore implements Datastore {
    private Map<String[], String> store;

    private final Logger logger = LoggerFactory.getLogger(DumbDatastore.class);


    public DumbDatastore() {
        logger.trace("Creating DumbDatastore");
        store = new HashMap<>();
    }

    @Override
    public String get(String[] key) {
        logger.trace("Getting key {}\nstore: {}", key, store);
        return store.get(key);
    }

    @Override
    public void set(String[] key, String value) {
        logger.trace("Setting key {} = {}\nstore: {}", key, value, store);
        store.put(key, value);
    }

    @Override
    public Set<String> zRangeLessThen(String[] key, long score) {
        return null;
    }

    @Override
    public void zRem(String[] key, String value) {

    }

    @Override
    public void zAdd(String[] key, long score, String value) {

    }
}
