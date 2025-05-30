package com.agonkolgeci.playze_family_bot.api;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.agonkolgeci.playze_family_bot.PlayzeFamilyBot;
import com.agonkolgeci.playze_family_bot.IComponent;
import com.agonkolgeci.playze_family_bot.config.ConfigController;
import com.agonkolgeci.playze_family_bot.database.DatabaseController;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public abstract class APIComponent extends IComponent {

    @Getter @NotNull protected final JDA api;

    @Getter @NotNull protected final EventWaiter eventWaiter;

    @Getter @NotNull protected final ConfigController configController;
    @Getter @NotNull protected final DatabaseController databaseController;

    public APIComponent(@NotNull PlayzeFamilyBot instance, @NotNull JDA api) {
        super(instance);

        this.api = api;

        this.eventWaiter = instance.getEventWaiter();

        this.configController = instance.getConfigController();
        this.databaseController = instance.getDatabaseController();
    }

}
