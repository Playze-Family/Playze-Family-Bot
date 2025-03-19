package fr.jielos.playzefamilybot.api.managers;

import org.jetbrains.annotations.NotNull;

public interface Controller<C extends Controller<C>> {

    @NotNull C load();

}
