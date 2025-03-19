package fr.jielos.playzefamilybot.api.events;

import fr.jielos.playzefamilybot.PlayzeFamilyBot;
import fr.jielos.playzefamilybot.api.APIComponent;
import fr.jielos.playzefamilybot.api.managers.Controller;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import org.jetbrains.annotations.NotNull;

public class EventsController extends APIComponent implements Controller<EventsController> {

    @Getter @NotNull private final IEventManager eventManager;

    public EventsController(@NotNull PlayzeFamilyBot instance, @NotNull JDA client) {
        super(instance, client);

        this.eventManager = api.getEventManager();
    }

    @NotNull
    public EventsController add(@NotNull Object... eventListeners) {
        for(@NotNull final Object eventListener : eventListeners) {
            this.registerEventAdapter(eventListener);
        }

        return this;
    }

    @NotNull
    public EventsController load() {
        PlayzeFamilyBot.getLogger().info("Successful loading of Listeners controller.");

        return this;
    }

    public void registerEventAdapter(@NotNull Object listener) {
        api.addEventListener(listener);
    }

    public void handleEvent(@NotNull GenericEvent event) {
        eventManager.handle(event);
    }

}
