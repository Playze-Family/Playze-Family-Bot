package fr.jielos.playzefamilybot.client.guilds.channels;

import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.settings.GuildSetting;
import fr.jielos.playzefamilybot.client.guilds.settings.SettingsController;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class ChannelsController extends GuildComponent implements Controller<ChannelsController> {

    @NotNull private final SettingsController settingsController;

    public ChannelsController(@NotNull GuildCache guildCache, @NotNull SettingsController settingsController) {
        super(guildCache);

        this.settingsController = settingsController;
    }

    @NotNull
    @Override
    public ChannelsController load() {
        instance.getEventsController().registerEventAdapter(this);

        return Controller.super.load();
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        @NotNull final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @NotNull final Member member = event.getMember();

        @Nullable final AudioChannel creatorChannel = settingsController.retrieveSetting(GuildSetting.GUILD_PUBLIC_VOICE_CHANNELS_CREATOR);
        if(creatorChannel == null) return;

        @Nullable final AudioChannel leftChannel = event.getOldValue();
        @Nullable final AudioChannel joinChannel = event.getNewValue();
        if(!canManageChannels(selfMember, joinChannel)) return;

        if(joinChannel != null && joinChannel == creatorChannel) {
            joinChannel.createCopy().setName(Objects.requireNonNull(settingsController.retrieveSetting(GuildSetting.GUILD_PUBLIC_VOICE_CHANNELS_NAME))).queue(newChannel -> {
                guild.moveVoiceMember(member, (AudioChannel) newChannel).queue();
            });
        }

        if(leftChannel != null && leftChannel != guild.getAfkChannel() && leftChannel != creatorChannel) {
            if(leftChannel.getMembers().isEmpty()) {
                leftChannel.delete().queue();
            }
        }
    }

    public boolean canManageChannels(@NotNull Member member, @NotNull GuildChannel... channels) {
        return member.hasPermission(Permission.MANAGE_CHANNEL) && Arrays.stream(channels).filter(Objects::nonNull).noneMatch(channel -> channel.getPermissionContainer().getMemberPermissionOverrides().stream().anyMatch(po -> Objects.equals(po.getMember(), member) && po.getDenied().contains(Permission.MANAGE_CHANNEL)));
    }
}