package com.agonkolgeci.gebo.api.cache;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface CacheDirectory {

    @NotNull File getCacheDirectory();

    @NotNull
    default File retrieveCacheFile(@NotNull String target) {
        return retrieveCacheFile(target, false);
    }

    @NotNull
    default File retrieveCacheFile(@NotNull String target, boolean deleteOnExit) {
        @NotNull final File cacheFile = new File(getCacheDirectory(), target);
        if(deleteOnExit) {
            cacheFile.deleteOnExit();
        }

        return cacheFile;
    }

}
