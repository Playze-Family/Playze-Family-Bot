package com.agonkolgeci.playze_family_bot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.agonkolgeci.playze_family_bot.IComponent;
import com.agonkolgeci.playze_family_bot.PlayzeFamilyBot;
import com.agonkolgeci.playze_family_bot.api.managers.Controller;
import com.agonkolgeci.playze_family_bot.config.ConfigController;
import com.agonkolgeci.playze_family_bot.database.function.SQLFunction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.ConfigurationException;
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

public class DatabaseController extends IComponent implements Controller<DatabaseController> {

    public static final int RECONNECTION_MAX_ATTEMPTS = 5;
    public static final int RECONNECTION_SLEEP_TIMEOUT = 3;

    @NotNull @Getter private final HikariConfig hikariConfig;
    @NotNull @Getter private final HikariDataSource hikariDataSource;

    public DatabaseController(@NotNull PlayzeFamilyBot instance, @NotNull ConfigController configController) throws ConfigurationException {
        super(instance);

        this.hikariConfig = retrieveDatabaseCredentials(configController).toHikariConfig();
        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @NotNull
    @Override
    public DatabaseController load() {
        try {
            this.executeSchema(Objects.requireNonNull(getClass().getResourceAsStream("/database/ini.sql"), "Can't find initialization SQL schema."));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlayzeFamilyBot.getLogger().info("Successful loading of Database controller !");

        return this;
    }

    @NotNull
    private DatabaseCredentials retrieveDatabaseCredentials(@NotNull ConfigController configController) throws ConfigurationException {
        @Nullable final String host = configController.getString("db.host");
        @Nullable final String username = configController.getString("db.username");
        @Nullable final String password = configController.getString("db.password");
        @Nullable final String name = configController.getString("db.name");
        final int maxPoolSize = configController.getInt("db.maxPoolSize");

        if(host == null || username == null || password == null || name == null || maxPoolSize <= 0) {
            throw new ConfigurationException("Unable to retrieve the database connection credentials from the configuration, check it. Remember to set maxPoolSize to 1 at least.");
        }

        return new DatabaseCredentials(host, username, password, name, maxPoolSize, 3306);
    }

    @NotNull
    public Connection getConnection() {
        int attempts = 0;

        while (attempts < RECONNECTION_MAX_ATTEMPTS) {
            try {
                return hikariDataSource.getConnection();
            } catch (SQLException sqlException) {
                attempts++;

                logger.error("Unable to recover the connection to the database, launching a new attempt in {} seconds...", RECONNECTION_SLEEP_TIMEOUT);

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
            throw new RuntimeException(String.format("Unable to prepare SQL statmement: %s", sql), exception);
        }
    }

    public void executeQuery(@NotNull String sql, @NotNull SQLFunction<ResultSet> rsProcessor, @NotNull Object... objects) {
        this.prepareStatement(sql, preparedStatement -> {
            try (@NotNull ResultSet resultSet = preparedStatement.executeQuery()) {
                rsProcessor.apply(resultSet);
            } catch (SQLException exception) {
                throw new RuntimeException(String.format("Unable to execute SQL query: %s", sql), exception);
            }
        }, objects);
    }

    public void executeUpdate(@NotNull String sql, @NotNull Object... objects) {
        this.prepareStatement(sql, preparedStatement -> {
            try {
                preparedStatement.executeUpdate();
            } catch (SQLException exception) {
                throw new RuntimeException(String.format("Unable to execute SQL update: %s", sql), exception);
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
