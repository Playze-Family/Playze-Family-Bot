package fr.jielos.playzefamilybot.client.guilds;

import fr.jielos.playzefamilybot.client.ClientComponent;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public abstract class GuildComponent extends ClientComponent {

    @Getter @NotNull protected final Guild guild;
    @Getter @NotNull protected final GuildCache guildCache;

    @Getter @NotNull protected final Member selfMember;

    public GuildComponent(@NotNull GuildCache guildCache) {
        super(guildCache.getClientCache());

        this.guild = guildCache.getGuild();
        this.guildCache = guildCache;

        this.selfMember = guild.getSelfMember();
    }

}
