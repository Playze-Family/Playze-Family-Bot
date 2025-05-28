package com.agonkolgeci.gebo.api.events.guild;

import com.agonkolgeci.gebo.core.settings.GuildSetting;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericGuildSettingUpdate extends GenericGuildEvent {

    @Getter @NotNull protected final GuildSetting<?> setting;
    @Getter @Nullable protected final Object newValue;

    public GenericGuildSettingUpdate(@NotNull JDA api, @NotNull Guild guild, @NotNull GuildSetting<?> setting, @Nullable Object newValue) {
        super(api, -1, guild);

        this.setting = setting;
        this.newValue = newValue;
    }

}
