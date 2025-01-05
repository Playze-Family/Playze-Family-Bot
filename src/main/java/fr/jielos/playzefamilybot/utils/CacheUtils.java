package fr.jielos.playzefamilybot.utils;

import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CacheUtils {

    public static final File CACHE_DIRECTORY = new File(".cache");

    public static boolean isCacheDirectory(@NotNull File target) {
        return target.getAbsolutePath().startsWith(CACHE_DIRECTORY.getAbsolutePath());
    }

    @NotNull
    public static File retrieveCacheDirectory(@NotNull File parent, boolean deleteOnExit, @NotNull Object... objects) {
        if(!isCacheDirectory(parent)) throw new IllegalStateException("The parent folder is not in the cache folder.");

        @NotNull final File directory = new File(parent, ObjectUtils.joinObjects("/", objects));
        if(!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Cannot create the following directory: " + directory.getPath());

        if(deleteOnExit) {
            directory.deleteOnExit();
        }

        return directory;
    }

    @NotNull
    public static File retrieveCacheDirectory(boolean deleteOnExit, @NotNull Object... objects) {
        return retrieveCacheDirectory(CACHE_DIRECTORY, deleteOnExit, objects);
    }

}
