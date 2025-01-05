package fr.jielos.playzefamilybot.api.cache;

public interface CacheConnector {

    void saveCache();

    default void deleteCache() {}

}
