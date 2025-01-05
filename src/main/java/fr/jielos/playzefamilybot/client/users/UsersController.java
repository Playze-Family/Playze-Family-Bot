package fr.jielos.playzefamilybot.client.users;

import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.client.ClientCache;
import fr.jielos.playzefamilybot.client.ClientComponent;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UsersController extends ClientComponent implements Controller<UsersController> {

    @Getter @NotNull private final Map<String, UserCache> users;

    public UsersController(@NotNull ClientCache clientCache) {
        super(clientCache);

        this.users = new HashMap<>();
    }

    @NotNull
    @Override
    public UsersController load() {
        instance.getEventsController().registerEventAdapter(this);

        return Controller.super.load("Successful loading of Users controller.");
    }

    @NotNull
    public UserCache retrieveUserCache(@NotNull User user) {
        return ObjectUtils.retrieveObjectOrElseGet(users, user.getId(), () -> {
             try {
                 return new UserCache(clientCache, user.getId());
             } catch (Exception exception) {
                 exception.printStackTrace();
             }

             return retrieveUserCache(user);
        });
    }
}
