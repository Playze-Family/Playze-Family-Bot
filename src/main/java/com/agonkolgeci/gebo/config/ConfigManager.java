package com.agonkolgeci.gebo.config;

import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.database.DatabaseCredentials;
import com.agonkolgeci.gebo.utils.common.objects.IntegerUtils;
import lombok.Getter;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    @Getter @NotNull private final Properties properties;

    public ConfigManager(@Nullable String path) throws IOException {
        this.properties = retrieveProperties(path);
    }

    @NotNull
    private Properties retrieveProperties(@Nullable String path) throws IOException {
        @NotNull final Properties properties = new Properties();
        @Nullable InputStream configuration = GeboBot.class.getClassLoader().getResourceAsStream("config.properties");

        if(path != null) {
            @NotNull final File providedFile = new File(path);
            if(!providedFile.exists()) throw new IllegalArgumentException("Unable to access to the provided configuration file.");

            configuration = new FileInputStream(providedFile);

            GeboBot.LOGGER.info("The provided configuration file has been loaded.");
        } else {
            @NotNull final File possibleFile = new File("config.properties");
            if(possibleFile.exists()) {
                configuration = new FileInputStream(possibleFile);

                GeboBot.LOGGER.info("A configuration file was found in the root and will be used.");
            }
        }

        Checks.check(configuration != null, "Unable to access the configuration: check the default configuration, and (if you specified a specific configuration); its path.");

        properties.load(configuration);

        return properties;
    }

    public @Nullable String getString(@NotNull String key) {
        return properties.getProperty(key, null);
    }

    public int getInt(@NotNull String key) {
        return IntegerUtils.parseInt(getString(key));
    }

    public @NotNull DatabaseCredentials getDbCredentials() throws ConfigurationException {
        @Nullable final String host = this.getString("db.host");
        @Nullable final String username = this.getString("db.username");
        @Nullable final String password = this.getString("db.password");
        @Nullable final String name = this.getString("db.name");
        final int maxPoolSize = this.getInt("db.maxPoolSize");

        if(host == null || username == null || password == null || name == null || maxPoolSize <= 0) {
            throw new ConfigurationException("Unable to retrieve the database connection credentials from the configuration, check it. Remember to set maxPoolSize to 1 at least.");
        }

        return new DatabaseCredentials(host, username, password, name, maxPoolSize, 3306);
    }

}
