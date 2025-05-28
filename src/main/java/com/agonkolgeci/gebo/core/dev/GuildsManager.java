package com.agonkolgeci.gebo.core.dev;

import com.agonkolgeci.gebo.BotAdapter;
import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.core.ClientCache;
import com.agonkolgeci.gebo.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GuildsManager implements BotAdapter {

    @NotNull private final JDA api;

    @Getter @NotNull private final Map<String, GuildCache> guilds;

    public GuildsManager(@NotNull JDA api) {
        this.api = api;

        this.guilds = new HashMap<>();
    }

    @Override
    public void load() {
        // TODO : do a controller for every modules. Guild Data is only for storing data in the guild not else.
//        clientCache.getApi().getGuilds().forEach(this::retrieveGuildCache);

        instance.getEventsController().registerEventAdapter(this);

        GeboBot.getLOGGER().info("Successful loading of Guilds controller.");
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