package fr.jielos.playzefamilybot.client;

import fr.jielos.playzefamilybot.api.APIComponent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class ClientComponent extends APIComponent {

    @Getter @NotNull protected final ClientCache clientCache;

    public ClientComponent(@NotNull ClientCache clientCache) {
        super(clientCache.getInstance(), clientCache.getApi());

        this.clientCache = clientCache;
    }

}
