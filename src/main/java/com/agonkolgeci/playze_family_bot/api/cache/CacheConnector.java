package com.agonkolgeci.playze_family_bot.api.cache;

public interface CacheConnector {

    void saveCache();

    default void deleteCache() {}

}
