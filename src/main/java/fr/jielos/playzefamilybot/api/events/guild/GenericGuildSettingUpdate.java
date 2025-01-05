package fr.jielos.playzefamilybot.api.events.guild;

import fr.jielos.playzefamilybot.client.guilds.settings.GuildSetting;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericGuildSettingUpdate extends GenericGuildEvent {

    @Getter @NotNull protected final GuildSetting guildSetting;
    @Getter @Nullable protected final Object newValue;

    public GenericGuildSettingUpdate(@NotNull JDA api, @NotNull Guild guild, @NotNull GuildSetting guildSetting, @Nullable Object newValue) {
        super(api, -1, guild);

        this.guildSetting = guildSetting;
        this.newValue = newValue;
    }

}
