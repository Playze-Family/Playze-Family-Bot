package com.agonkolgeci.playze_family_bot.client;

import com.agonkolgeci.playze_family_bot.PlayzeFamilyBot;
import com.agonkolgeci.playze_family_bot.api.APIComponent;
import com.agonkolgeci.playze_family_bot.api.managers.Controller;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildsController;
import com.agonkolgeci.playze_family_bot.client.presence.PresenceController;
import com.agonkolgeci.playze_family_bot.client.users.UsersController;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public class ClientCache extends APIComponent implements Controller<ClientCache> {

    @Getter @NotNull private final PresenceController presenceController;

    @Getter @NotNull private final UsersController usersController;
    @Getter @NotNull private final GuildsController guildsController;

    public ClientCache(@NotNull PlayzeFamilyBot instance, @NotNull JDA api) {
        super(instance, api);

        this.presenceController = new PresenceController(this).load();

        this.usersController = new UsersController(this).load();
        this.guildsController = new GuildsController(this).load();
    }

    @NotNull
    @Override
    public ClientCache load() {
        presenceController.update();
        instance.getCommandsController().deleteUnregisteredCommands();

        return this;
    }
}
