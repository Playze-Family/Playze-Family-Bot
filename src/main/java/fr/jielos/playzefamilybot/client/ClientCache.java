package fr.jielos.playzefamilybot.client;

import fr.jielos.playzefamilybot.PlayzeFamilyBot;
import fr.jielos.playzefamilybot.api.APIComponent;
import fr.jielos.playzefamilybot.client.guilds.GuildsController;
import fr.jielos.playzefamilybot.client.presence.PresenceController;
import fr.jielos.playzefamilybot.client.users.UsersController;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public class ClientCache extends APIComponent {

    @Getter @NotNull private final PresenceController presenceController;

    @Getter @NotNull private final UsersController usersController;
    @Getter @NotNull private final GuildsController guildsController;

    public ClientCache(@NotNull PlayzeFamilyBot instance, @NotNull JDA api) {
        super(instance, api);

        this.presenceController = new PresenceController(this).load();

        this.usersController = new UsersController(this).load();
        this.guildsController = new GuildsController(this).load();
    }

}
