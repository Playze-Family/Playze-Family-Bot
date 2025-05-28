package com.agonkolgeci.gebo.database;

import com.agonkolgeci.gebo.BotAdapter;
import com.agonkolgeci.gebo.GeboBot;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatabaseManager implements BotAdapter {

    public static final int RECONNECTION_MAX_ATTEMPTS = 5;
    public static final int RECONNECTION_SLEEP_TIMEOUT = 3;

    @NotNull @Getter private final HikariConfig hikariConfig;
    @NotNull @Getter private final HikariDataSource hikariDataSource;

    public DatabaseManager(@NotNull DatabaseCredentials credentials) {
        this.hikariConfig = credentials.toHikariConfig();
        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void load() {
        try {
            this.executeSchema(Objects.requireNonNull(GeboBot.class.getResourceAsStream("/database/ini.sql"), "Can't find initialization SQL schema."));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        GeboBot.LOGGER.info("Database is ready.");
    }

    @NotNull
    public Connection getConnection() {
        int attempts = 0;

        while (attempts < RECONNECTION_MAX_ATTEMPTS) {
            try {
                return hikariDataSource.getConnection();
            } catch (SQLException sqlException) {
                attempts++;

                GeboBot.LOGGER.error("Unable to recover the connection to the database, launching a new attempt in {} seconds...", RECONNECTION_SLEEP_TIMEOUT);

                try {
                    Thread.sleep(RECONNECTION_SLEEP_TIMEOUT * 1000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();

                    throw new RuntimeException("Thread was interrupted during database reconnection attempts", interruptedException);
                }
            }
        }

        throw new RuntimeException(String.format("Failed to connect to the database after %d attempts.", attempts));
    }

    private void prepareStatement(@NotNull String sql, @NotNull SQLFunction<PreparedStatement> psProcessor, @NotNull Object... objects) {
        try (@NotNull final Connection connection = this.getConnection();  @NotNull final PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            for (int i = 0; i < objects.length; i++) {
                preparedStatement.setObject(i + 1, objects[i]);
            }

            psProcessor.apply(preparedStatement);
        } catch (SQLException exception) {
            GeboBot.LOGGER.error("Unable to execute SQL statement", exception);
        }
    }

    public void executeQuery(@NotNull String sql, @NotNull SQLFunction<ResultSet> rsProcessor, @NotNull Object... objects) {
        this.prepareStatement(sql, preparedStatement -> {
            try (@NotNull ResultSet resultSet = preparedStatement.executeQuery()) {
                rsProcessor.apply(resultSet);
            } catch (SQLException exception) {
                GeboBot.LOGGER.error("Unable to execute SQL query", exception);
            }
        }, objects);
    }

    public void executeUpdate(@NotNull String sql, @NotNull Object... objects) {
        this.prepareStatement(sql, preparedStatement -> {
            try {
                preparedStatement.executeUpdate();
            } catch (SQLException exception) {
                GeboBot.LOGGER.error("Unable to execute SQL update", exception);
            }
        }, objects);
    }

    public void executeSchema(@NotNull InputStream inputStream) throws IOException {
        @NotNull final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        @NotNull final StringBuilder sqlBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("--") || line.startsWith("#")) continue;

            sqlBuilder.append(line).append("\n");

            if(line.trim().endsWith(";")) {
                this.executeUpdate(sqlBuilder.toString());

                sqlBuilder.setLength(0);
            }
        }

        reader.close();
    }

}
