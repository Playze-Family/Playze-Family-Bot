package fr.jielos.playzefamilybot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.jielos.playzefamilybot.IComponent;
import fr.jielos.playzefamilybot.PlayzeFamilyBot;
import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.config.ConfigController;
import fr.jielos.playzefamilybot.database.function.SQLFunction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.naming.ConfigurationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseController extends IComponent implements Controller<DatabaseController> {

    public static final int MAX_CONNECTIONS_ATTEMPTS = 5;

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
        return Controller.super.load("Successful database credentials loaded !");
    }

    @NotNull
    private DatabaseCredentials retrieveDatabaseCredentials(@NotNull ConfigController configController) throws ConfigurationException {
        final String host = configController.retrieveValue("db.host");
        final String username = configController.retrieveValue("db.username");
        final String password = configController.retrieveValue("db.password");
        final String name = configController.retrieveValue("db.name");
        final int maxPoolSize = configController.retrieveInt("db.maxPoolSize");

        if(host == null || username == null || password == null || name == null) {
            throw new ConfigurationException("Unable to retrieve the database connection credentials from the configuration, check it.");
        }

        return new DatabaseCredentials(host, username, password, name, maxPoolSize, 3306);
    }

    @NotNull
    public Connection getConnection() {
        int attempts = 0;

        while (attempts < MAX_CONNECTIONS_ATTEMPTS) {
            try {
                return hikariDataSource.getConnection();
            } catch (SQLException sqlException) {
                attempts++;

                logger.error("Unable to recover the connection to the database, launching a new attempt in 2 seconds...");

                try {
                    Thread.sleep(2000);
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

}
