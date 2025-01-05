package fr.jielos.playzefamilybot.client.users;

import fr.jielos.playzefamilybot.client.ClientComponent;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public abstract class UserComponent extends ClientComponent {

    @Getter @NotNull protected final User user;
    @Getter @NotNull protected final UserCache userCache;

    public UserComponent(@NotNull UserCache userCache) {
        super(userCache.getClientCache());

        this.user = userCache.getUser();
        this.userCache = userCache;
    }

}
