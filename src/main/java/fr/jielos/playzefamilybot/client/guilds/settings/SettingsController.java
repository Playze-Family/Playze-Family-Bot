package fr.jielos.playzefamilybot.client.guilds.settings;

import fr.jielos.playzefamilybot.api.cache.CacheConnector;
import fr.jielos.playzefamilybot.api.events.guild.GenericGuildSettingUpdate;
import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.settings.adapters.CommandSettings;
import fr.jielos.playzefamilybot.client.guilds.settings.adapters.CommandStats;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public class SettingsController extends GuildComponent implements Controller<SettingsController>, CacheConnector {

    @Getter @NotNull private final Map<GuildSetting, Object> settings;

    public SettingsController(@NotNull GuildCache guildCache) {
        super(guildCache);

        this.settings = new HashMap<>();
    }

    @NotNull
    @Override
    public SettingsController load() {
        databaseController.executeQuery("SELECT * FROM guilds_settings WHERE guild_id = ?", settings -> {
            while (settings.next()) {
                @Nullable final GuildSetting setting = ObjectUtils.fetchObject(GuildSetting.class, settings.getString("setting_name"));
                if(setting == null) {
                    settings.deleteRow();

                    continue;
                }

                @NotNull final String value = settings.getString("value");

                @Nullable final Object object = this.initSetting(setting, value);
                if(object == null) {
                    settings.deleteRow();
                }
            }
        },  guild.getId());

        instance.getCommandsController().registerCommandAdapter(new CommandSettings(guildCache, this));
        instance.getCommandsController().registerCommandAdapter(new CommandStats(guildCache, this));

        return this;
    }

    @Nullable
    public <T> T initSetting(@NotNull GuildSetting setting, @NotNull String value) {
        @Nullable final Object object = setting.getObjectType().parse(guild, value);
        if(object == null) return null;

        this.settings.put(setting, object);

        return (T) object;
    }

    @NotNull
    public <T> T setSetting(@NotNull GuildSetting setting, @NotNull String newValue) {
        @Nullable final Object newObject = this.initSetting(setting, newValue);
        Checks.check(newObject != null, "Le paramètre [%s] requiert une valeur correspondant au type suivant: `%s`", setting.getDisplayName(), setting.getObjectType().getDisplayName());

        this.saveCache();

        instance.getEventsController().handleEvent(new GenericGuildSettingUpdate(api, guild, setting, newObject));

        return (T) newObject;
    }

    public void deleteSetting(@NotNull GuildSetting setting)  {
        if(!settings.containsKey(setting)) return;

        this.settings.remove(setting);
        this.deleteCache(setting);

        instance.getEventsController().handleEvent(new GenericGuildSettingUpdate(api, guild, setting, null));
    }

    public boolean hasSetting(@NotNull GuildSetting setting) {
        return settings.containsKey(setting);
    }

    @Nullable
    public <T> T retrieveSetting(@NotNull GuildSetting setting) {
        return (T) settings.getOrDefault(setting, setting.getDefaultValue());
    }

    @NotNull
    public <T> T retrieveSetting(@NotNull GuildSetting setting, @NotNull T t) {
        return Objects.requireNonNullElse(retrieveSetting(setting), t);
    }

    @Override
    public void saveCache() {
        for(@NotNull final Map.Entry<GuildSetting, Object> entry : settings.entrySet()) {
            @NotNull final GuildSetting setting = entry.getKey();
            @NotNull final Object object = entry.getValue();

            CompletableFuture.runAsync(() -> {
                databaseController.executeUpdate("INSERT INTO guilds_settings(guild_id, setting_name, value) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)", guild.getId(), setting.toString(), setting.getObjectType().toRegistrable(object));
            });
        }
    }

    public void deleteCache(@NotNull GuildSetting setting) {
        CompletableFuture.runAsync(() -> {
            databaseController.executeUpdate("DELETE FROM guilds_settings WHERE guild_id = ? AND setting_name = ?", guild.getId(), setting.toString());
        });
    }
}
