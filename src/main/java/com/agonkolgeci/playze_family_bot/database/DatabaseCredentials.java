package com.agonkolgeci.playze_family_bot.database;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

public record DatabaseCredentials(@NotNull String host, @NotNull String username, @NotNull String password, @NotNull String name, int maxPoolSize, int port) {

    @NotNull
    public String toURI() {
        return "jdbc:mysql://" + host + ":" + port + "/" + name;
    }

    @NotNull
    public HikariConfig toHikariConfig() {
        @NotNull final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(toURI());
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(maxPoolSize);

        return hikariConfig;
    }

}
