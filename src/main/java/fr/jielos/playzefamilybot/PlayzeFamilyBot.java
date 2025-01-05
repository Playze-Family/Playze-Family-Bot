package fr.jielos.playzefamilybot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import fr.jielos.playzefamilybot.api.commands.CommandsController;
import fr.jielos.playzefamilybot.api.events.EventsController;
import fr.jielos.playzefamilybot.client.ClientCache;
import fr.jielos.playzefamilybot.config.ConfigController;
import fr.jielos.playzefamilybot.database.DatabaseController;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayzeFamilyBot {

    @Getter private static Logger logger;

    @Getter private static PlayzeFamilyBot instance;

    public static void main(String[] args) throws Exception{
        logger = LoggerFactory.getLogger(PlayzeFamilyBot.class);

        instance = new PlayzeFamilyBot();
    }

    @Getter @NotNull private final ConfigController configController;
    @Getter @NotNull private final DatabaseController databaseController;

    @Getter @Nullable private final String clientToken;
    @Getter @Nullable private final String clientID;

    @Getter @NotNull private final JDA api;
    @Getter @NotNull private final EventWaiter eventWaiter;

    @Getter @NotNull private final EventsController eventsController;
    @Getter @NotNull private final CommandsController commandsController;

    @Getter @NotNull private final ClientCache clientCache;

    public PlayzeFamilyBot() throws Exception {
        logger.info("Starting the client, please wait...");

        this.configController = new ConfigController(this, System.getProperty("config")).load();
        this.databaseController = new DatabaseController(this, configController).load();

        this.clientToken = configController.getString("client.token");
        this.clientID = configController.getString("client.id");

        this.api = this.createDefaultJDA().awaitReady();
        this.eventWaiter = new EventWaiter();

        this.eventsController = new EventsController(this, api).load(eventWaiter);
        this.commandsController = new CommandsController(this, api, eventsController).load();

        this.clientCache = new ClientCache(this, api);
        this.clientCache.getPresenceController().updatePresence();
        this.clientCache.getInstance().getCommandsController().deleteUnregisteredCommands();

        logger.info("Application startup completed successfully.");
    }

    @NotNull
    private JDA createDefaultJDA() {
        Checks.notNull(clientToken, "No client token provided.");
        Checks.notNull(clientID, "No client id provided.");

        return JDABuilder.create(clientToken, GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.playing("Notre agent virtuel se déploie en mode furtif. Préparez-vous à pénétrer le monde secret de la conversation automatisée."))
                .build();
    }

}
