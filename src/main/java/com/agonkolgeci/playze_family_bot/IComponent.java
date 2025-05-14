package com.agonkolgeci.playze_family_bot;

import com.agonkolgeci.playze_family_bot.api.events.PListenerAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class IComponent extends PListenerAdapter {

    @Getter @NotNull protected final PlayzeFamilyBot instance;

    @Getter @NotNull protected final Logger logger;

    public IComponent(@NotNull PlayzeFamilyBot instance) {
        this.instance = instance;

        this.logger = PlayzeFamilyBot.getLogger();
    }

}
