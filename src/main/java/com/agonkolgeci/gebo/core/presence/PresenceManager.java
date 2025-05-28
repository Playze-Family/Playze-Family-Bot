package com.agonkolgeci.gebo.core.presence;

import com.agonkolgeci.gebo.BotAdapter;
import com.agonkolgeci.gebo.GeboBot;
import net.dv8tion.jda.api.OnlineStatus;
import org.jetbrains.annotations.NotNull;

public class PresenceManager implements BotAdapter {

    @NotNull private final GeboBot instance;

    public PresenceManager(@NotNull GeboBot instance) {
        this.instance = instance;
    }

    @Override
    public void load() {
        instance.getApi().addEventListener(this);

        instance.getApi().getPresence().setPresence(OnlineStatus.ONLINE, null, true);

        GeboBot.LOGGER.info("Bot connected on Discord under the following account: @{}.", instance.getApi().getSelfUser().getEffectiveName());
    }

}
