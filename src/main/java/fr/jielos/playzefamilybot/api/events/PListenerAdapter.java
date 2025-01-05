package fr.jielos.playzefamilybot.api.events;

import fr.jielos.playzefamilybot.api.events.guild.GenericGuildSettingUpdate;
import fr.jielos.playzefamilybot.api.events.member.GenericGuildMemberProfileUpdate;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public abstract class PListenerAdapter extends ListenerAdapter {

    public void onGuildSettingChange(@NotNull GenericGuildSettingUpdate event) {}

    public void onGuildMemberProfileUpdate(@NotNull GenericGuildMemberProfileUpdate event) {}

    @Override
    public void onGenericEvent(@NotNull GenericEvent e) {
        if(e instanceof final GenericGuildSettingUpdate event) onGuildSettingChange(event);
        if(e instanceof final GenericGuildMemberProfileUpdate event) onGuildMemberProfileUpdate(event);
    }

}
