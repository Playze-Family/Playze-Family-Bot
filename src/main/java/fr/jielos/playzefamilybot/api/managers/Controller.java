package fr.jielos.playzefamilybot.api.managers;

import fr.jielos.playzefamilybot.PlayzeFamilyBot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public interface Controller<C extends Controller<C>> {

    @NotNull
     default C load() {
        return load(null);
    }

    @NotNull
    default C load(@Nullable String message, @NotNull Object... objects) {
        if(message != null) {
            PlayzeFamilyBot.getLogger().info(message, objects);
        }

        return (C) this;
    }

}
