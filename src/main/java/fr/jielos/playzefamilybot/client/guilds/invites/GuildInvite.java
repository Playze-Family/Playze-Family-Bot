package fr.jielos.playzefamilybot.client.guilds.invites;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class GuildInvite {

    @Getter @NotNull private final Date date;
    @Getter @NotNull private final String code;

    public GuildInvite(@NotNull Date date, @NotNull String code) {
        this.date = date;
        this.code = code;
    }
}