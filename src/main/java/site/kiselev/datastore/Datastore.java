package site.kiselev.datastore;

import java.util.Set;

/**
 * {@link Datastore} interface
 */
public interface Datastore {

    String get(String[] key);

    void set(String[] key, String value);

    void zAdd(String[] key, long score, String value);

    Set<String> zRangeLessThen(String[] key, long score);

    void zRem(String[] key, String value);


}
