package fr.jielos.playzefamilybot.config;

import fr.jielos.playzefamilybot.IComponent;
import fr.jielos.playzefamilybot.PlayzeFamilyBot;
import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.utils.common.objects.IntegerUtils;
import lombok.Getter;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigController extends IComponent implements Controller<ConfigController> {

    @Getter @NotNull private final Properties properties;

    public ConfigController(@NotNull PlayzeFamilyBot instance, @Nullable String path) throws IOException {
        super(instance);

        this.properties = retrieveProperties(path);
    }

    @NotNull
    public ConfigController load() {
        PlayzeFamilyBot.getLogger().info(("Loading of current client configuration completed!"));

        return this;
    }

    @NotNull
    private Properties retrieveProperties(@Nullable String path) throws IOException {
        @NotNull final Properties properties = new Properties();
        @Nullable InputStream configuration = getClass().getClassLoader().getResourceAsStream("config.properties");

        if(path != null) {
            @NotNull final File providedFile = new File(path);
            if(!providedFile.exists()) throw new IllegalArgumentException("Unable to access to the provided configuration file.");

            configuration = new FileInputStream(providedFile);

            logger.info("The provided configuration file has been loaded.");
        } else {
            @NotNull final File possibleFile = new File("config.properties");
            if(possibleFile.exists()) {
                configuration = new FileInputStream(possibleFile);

                logger.info("A configuration file was found in the root and will be used.");
            }
        }

        Checks.check(configuration != null, "Unable to access the configuration: check the default configuration, and (if you specified a specific configuration); its path.");

        properties.load(configuration);

        return properties;
    }

    @Nullable
    public String getString(@NotNull String key) {
        return properties.getProperty(key, null);
    }

    public int getInt(@NotNull String key) {
        return IntegerUtils.parseInt(getString(key));
    }

}
