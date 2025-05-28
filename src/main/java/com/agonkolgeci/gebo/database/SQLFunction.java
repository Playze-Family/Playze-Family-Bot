package com.agonkolgeci.gebo.database;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T> {

    void apply(T t) throws SQLException;

}
