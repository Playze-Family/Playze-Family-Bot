package com.agonkolgeci.gebo.core.channels;

import com.agonkolgeci.gebo.BotAdapter;
import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.core.settings.GuildSetting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ChannelsController implements BotAdapter {

    @NotNull private final GeboBot instance;

    public ChannelsController(@NotNull GeboBot instance) {
        this.instance = instance;
    }

    @Override
    public void load() {
        instance.getApi().addEventListener(this);
    }

    @SubscribeEvent
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        @NotNull final Guild guild = event.getGuild();
        @NotNull final Member member = event.getMember();

        final CompletableFuture<VoiceChannel> creatorChannelFuture = instance.getSettingsManager().getSettings(guild).getSetting(GuildSetting.AUTO_CHANNELS_CREATOR_CHANNEL);
        final CompletableFuture<String> creatorChannelNameFuture = instance.getSettingsManager().getSettings(guild).getSetting(GuildSetting.AUTO_CHANNELS_CREATE_NAME);

        creatorChannelFuture.thenCombine(creatorChannelNameFuture, (creatorChannel, creatorChannelName) -> {
            if(creatorChannel == null || creatorChannelName == null) return null;

            @Nullable final AudioChannel leftChannel = event.getOldValue();
            @Nullable final AudioChannel joinChannel = event.getNewValue();
            if(!canManageChannels(guild, joinChannel)) return null;

            if(joinChannel != null && joinChannel == creatorChannel) {
                joinChannel.createCopy().setName(creatorChannelName).queue(newChannel -> {
                    guild.moveVoiceMember(member, (AudioChannel) newChannel).queue();
                });
            }

            if(leftChannel != null && leftChannel.getName().equals(creatorChannelName) && leftChannel != creatorChannel) {
                if(leftChannel.getMembers().isEmpty()) {
                    leftChannel.delete().queue();
                }
            }

            return null;
        });
    }

    public static boolean canManageChannels(@NotNull Guild guild, @NotNull GuildChannel... channels) {
        @NotNull final Member selfMember = guild.getSelfMember();

        return selfMember.hasPermission(Permission.MANAGE_CHANNEL) &&
                Arrays.stream(channels)
                        .filter(Objects::nonNull)
                        .noneMatch(channel -> {
                            return guild.getAfkChannel() != channel && channel.getPermissionContainer().getMemberPermissionOverrides().stream()
                                    .anyMatch(po -> Objects.equals(po.getMember(), selfMember) && po.getDenied().contains(Permission.MANAGE_CHANNEL));
                        });
    }
}
