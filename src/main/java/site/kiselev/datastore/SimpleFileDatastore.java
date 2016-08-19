package site.kiselev.datastore;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File && Directories implementation of Datastore
 * Simplified version - without cache
 */
@SuppressWarnings("WeakerAccess")
public class SimpleFileDatastore implements Datastore {

    private final String BASE_PATH;
    private final String FILE_SUFFIX;

    public SimpleFileDatastore(String prefix, String suffix) {
        BASE_PATH = prefix;
        FILE_SUFFIX = suffix;
    }

    @Override
    public String get(String[] key) {

        String filename =
                BASE_PATH + File.separator + String.join(File.separator, (CharSequence[]) key)
                + FILE_SUFFIX;
        String result;

        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            result = stream.collect(Collectors.joining());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public void set(String[] key, String value) {
        String filename =
                BASE_PATH + File.separator + String.join(File.separator, (CharSequence[]) key)
                + FILE_SUFFIX;
        Path path = Paths.get(filename);

        if (!Files.exists(path.getParent()))
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException ignored) {}

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
