package com.agonkolgeci.playze_family_bot.database.function;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T> {

    void apply(T t) throws SQLException;

}