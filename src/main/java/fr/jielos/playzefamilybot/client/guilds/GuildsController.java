package fr.jielos.playzefamilybot.client.guilds;

import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.client.ClientCache;
import fr.jielos.playzefamilybot.client.ClientComponent;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GuildsController extends ClientComponent implements Controller<GuildsController> {

    @Getter @NotNull private final Map<String, GuildCache> guilds;

    public GuildsController(@NotNull ClientCache clientCache) {
        super(clientCache);

        this.guilds = new HashMap<>();
    }

    @NotNull
    @Override
    public GuildsController load() {
        clientCache.getApi().getGuilds().forEach(this::retrieveGuildCache);

        instance.getEventsController().registerEventAdapter(this);

        return Controller.super.load("Successful loading of Guilds controller.");
    }

    @NotNull
    public GuildCache retrieveGuildCache(@NotNull Guild guild) {
        return ObjectUtils.retrieveObjectOrElseGet(guilds, guild.getId(), () -> {
            try {
                return new GuildCache(clientCache, guild.getId());
            } catch (Exception exception) {
                throw new RuntimeException(String.format("Unable to retrieve guild: %s", guild.getId()), exception);
            }
        });
    }

}