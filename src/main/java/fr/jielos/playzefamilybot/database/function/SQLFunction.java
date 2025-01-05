package fr.jielos.playzefamilybot.database.function;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T> {

    void apply(T t) throws SQLException;

}