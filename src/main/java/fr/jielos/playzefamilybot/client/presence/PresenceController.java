package fr.jielos.playzefamilybot.client.presence;

import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.client.ClientCache;
import fr.jielos.playzefamilybot.client.ClientComponent;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.managers.Presence;
import org.jetbrains.annotations.NotNull;

public class PresenceController extends ClientComponent implements Controller<PresenceController> {

    @NotNull public static final String READY_STATUS_FORMAT = ("%d utilisateurs");

    @Getter @NotNull private final Presence presence;

    public PresenceController(@NotNull ClientCache clientCache) {
        super(clientCache);

        this.presence = api.getPresence();
    }

    @NotNull
    @Override
    public PresenceController load() {
        instance.getEventsController().registerEventAdapter(this);

        return Controller.super.load("Client connected on Discord under the following account: @{}.", api.getSelfUser().getEffectiveName());
    }

    public void updatePresence() {
        presence.setStatus(OnlineStatus.ONLINE);
        presence.setActivity(Activity.streaming(String.format(READY_STATUS_FORMAT, getTotalHumans()), ObjectUtils.EXTERNAL_STREAM_URL));

        logger.info("Client's status updated: '{}'", presence.getStatus());
        logger.info("Client's activity updated: '{}'", presence.getActivity());
    }

    public int getTotalHumans() {
        return (int) api.getUsers().stream().filter(user -> !user.isBot()).count();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        updatePresence();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        updatePresence();
    }
}
