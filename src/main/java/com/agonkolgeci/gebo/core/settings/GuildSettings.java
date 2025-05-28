package com.agonkolgeci.gebo.core.settings;

import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.api.events.guild.GenericGuildSettingUpdate;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unchecked")
public class GuildSettings {

    @NotNull private final GeboBot instance;
    @NotNull private final String guildId;

    @NotNull private final Map<GuildSetting<?>, Object> entries;

    public GuildSettings(@NotNull GeboBot instance, @NotNull String guildId) {
        this.instance = instance;
        this.guildId = guildId;

        this.entries = new ConcurrentHashMap<>();
    }

    public @NotNull Guild getGuild() {
        return Objects.requireNonNull(instance.getApi().getGuildById(guildId), "Guild not found");
    }

    public <T> CompletableFuture<T> getSetting(@NotNull GuildSetting<T> setting) {
        return CompletableFuture.supplyAsync(() -> {
            return (T) entries.computeIfAbsent(setting, k -> {
                final AtomicReference<T> result = new AtomicReference<>(setting.getDefaultValue());

                GeboBot.getDatabaseManager().executeQuery("SELECT value FROM guilds_settings WHERE guild_id = ? AND `key` = ?", rs -> {
                    if(rs.next()) {
                        result.set(setting.serialize(this.getGuild(), rs.getString("value")));
                    }
                }, guildId, setting.getKey());

                return result.get();
            });
        });
    }

    public void setSetting(@NotNull GuildSetting<?> setting, @NotNull Object value) {
        CompletableFuture.runAsync(() -> {
            entries.put(setting, value);

            instance.getApi().getEventManager().handle(new GenericGuildSettingUpdate(instance.getApi(), this.getGuild(), setting, value));

            // key is a reserved word
            GeboBot.getDatabaseManager().executeUpdate("INSERT INTO guilds_settings(guild_id, `key`, value) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)", guildId, setting.getKey(), ((GuildSetting<Object>) setting).deserialize(this.getGuild(), value));
        });
    }

    public void removeSetting(@NotNull GuildSetting<?> setting) {
        CompletableFuture.runAsync(() -> {
            entries.remove(setting);

            instance.getApi().getEventManager().handle(new GenericGuildSettingUpdate(instance.getApi(), this.getGuild(), setting, null));

            GeboBot.getDatabaseManager().executeUpdate("DELETE FROM guilds_settings WHERE guild_id = ? AND `key` = ?", guildId, setting.getKey());
        });
    }

}
