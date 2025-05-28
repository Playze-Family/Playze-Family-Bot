package com.agonkolgeci.gebo;

import com.agonkolgeci.gebo.config.ConfigManager;
import com.agonkolgeci.gebo.core.channels.ChannelsController;
import com.agonkolgeci.gebo.core.commands.CommandsManager;
import com.agonkolgeci.gebo.core.presence.PresenceManager;
import com.agonkolgeci.gebo.core.redirections.RedirectionsManager;
import com.agonkolgeci.gebo.core.settings.SettingsManager;
import com.agonkolgeci.gebo.database.DatabaseManager;
import com.google.gson.Gson;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.SplittableRandom;

@Getter
public class GeboBot implements BotAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(GeboBot.class);
    public static final SplittableRandom SPLITTABLE_RANDOM = new SplittableRandom();
    public static final Gson GSON = new Gson();

    @Getter private static ConfigManager configManager;
    @Getter private static DatabaseManager databaseManager;

    @Getter private static GeboBot instance;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting the bot, please wait...");

        configManager = new ConfigManager(System.getProperty("config"));

        databaseManager = new DatabaseManager(configManager.getDbCredentials());
        databaseManager.load();

        instance = new GeboBot();
        instance.load();

        LOGGER.info("Bot is now ready.");
    }

    @NotNull private final JDA api;

    @NotNull private final CommandsManager commandsManager;
    @NotNull private final ChannelsController channelsController;
    @NotNull private final SettingsManager settingsManager;
    @NotNull private final RedirectionsManager redirectionsManager;
    @NotNull private final PresenceManager presenceManager;

    public GeboBot() throws Exception {
        this.api = this.createJDA(Objects.requireNonNull(configManager.getString("client.token"), "Please provided a client token in the configuration.")).awaitReady();

        this.commandsManager = new CommandsManager(this);
        this.channelsController = new ChannelsController(this);
        this.settingsManager = new SettingsManager(this);
        this.redirectionsManager = new RedirectionsManager(this);
        this.presenceManager = new PresenceManager(this);
    }

    @Override
    public void load() {
        this.commandsManager.load();
        this.channelsController.load();
        this.settingsManager.load();
        this.redirectionsManager.load();
        this.presenceManager.load();
    }

    @NotNull
    private JDA createJDA(@NotNull String token) {
        return JDABuilder.create(token, GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .setEventManager(new AnnotatedEventManager())
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.playing("Notre agent virtuel se déploie en mode furtif. Préparez-vous à pénétrer le monde secret de la conversation automatisée !"))
                .build();
    }
}
