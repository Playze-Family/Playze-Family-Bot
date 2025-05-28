package com.agonkolgeci.gebo.core.settings;

import com.agonkolgeci.gebo.BotAdapter;
import com.agonkolgeci.gebo.GeboBot;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SettingsManager implements BotAdapter {

    @NotNull private final GeboBot instance;

    @NotNull private final Map<String, GuildSettings> settings;

    public SettingsManager(@NotNull GeboBot instance) {
        this.instance = instance;
        this.settings = new HashMap<>();
    }

    @Override
    public void load() {
        GeboBot.LOGGER.info("Settings are ready.");
    }

    @NotNull
    public GuildSettings getSettings(@NotNull Guild guild) {
        return settings.computeIfAbsent(guild.getName(), k -> new GuildSettings(instance, guild.getId()));
    }

}
