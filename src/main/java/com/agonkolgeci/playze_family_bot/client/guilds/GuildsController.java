package com.agonkolgeci.playze_family_bot.client.guilds;

import com.agonkolgeci.playze_family_bot.PlayzeFamilyBot;
import com.agonkolgeci.playze_family_bot.api.managers.Controller;
import com.agonkolgeci.playze_family_bot.client.ClientCache;
import com.agonkolgeci.playze_family_bot.client.ClientComponent;
import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
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

        PlayzeFamilyBot.getLogger().info("Successful loading of Guilds controller.");

        return this;
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