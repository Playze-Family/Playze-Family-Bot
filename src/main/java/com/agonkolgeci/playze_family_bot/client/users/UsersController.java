package com.agonkolgeci.playze_family_bot.client.users;

import com.agonkolgeci.playze_family_bot.PlayzeFamilyBot;
import com.agonkolgeci.playze_family_bot.api.managers.Controller;
import com.agonkolgeci.playze_family_bot.client.ClientCache;
import com.agonkolgeci.playze_family_bot.client.ClientComponent;
import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
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

        PlayzeFamilyBot.getLogger().info("Successful loading of Users controller.");

        return this;
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
