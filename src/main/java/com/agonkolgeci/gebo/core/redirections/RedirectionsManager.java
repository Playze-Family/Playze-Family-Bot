package com.agonkolgeci.gebo.core.redirections;

import com.agonkolgeci.gebo.BotAdapter;
import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.core.settings.GuildSetting;
import com.agonkolgeci.gebo.utils.common.ResourceUtils;
import com.agonkolgeci.gebo.utils.common.ui.EmojiUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RedirectionsManager implements BotAdapter {

    @NotNull private final GeboBot instance;

    @NotNull private final Map<String, Message> messages;

    public RedirectionsManager(@NotNull GeboBot instance) {
        this.instance = instance;

        this.messages = new HashMap<>();
    }

    @Override
    public void load() {
        instance.getApi().addEventListener(this);
    }

    @SubscribeEvent
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        @NotNull final Guild guild = event.getGuild();
        @NotNull final User author = event.getAuthor();
        if(author.isBot()) return;

        @NotNull final Channel channel = event.getChannel();
        @NotNull final Message message = event.getMessage();

        final CompletableFuture<TextChannel> receiverChannelFuture = instance.getSettingsManager().getSettings(guild).getSetting(GuildSetting.REDIRECTIONS_RECEIVER);
        final CompletableFuture<TextChannel> redirectChannelFuture = instance.getSettingsManager().getSettings(guild).getSetting(GuildSetting.REDIRECTIONS_SPEAKING_TARGET);

        receiverChannelFuture.thenCombine(redirectChannelFuture, (receiverChannel, redirectChannel) -> {
            if(receiverChannel == null || !channel.getId().equals(receiverChannel.getId())) return null;
            if(redirectChannel == null) return null;

            redirectChannel.sendMessage(message.getContentRaw()).setFiles(ResourceUtils.retrieveAttachmentsFiles(message.getAttachments())).setStickers(message.getStickers()).queue(redirectedMessage -> {
                message.addReaction(EmojiUtils.YES).queue();

                messages.put(message.getId(), redirectedMessage);
            }, error -> {
                message.addReaction(EmojiUtils.NO).queue();
            });

            return null;
        });
    }

    @SubscribeEvent
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        @NotNull final Message originalMessage = event.getMessage();
        if(!messages.containsKey(originalMessage.getId())) return;

        try {
            messages.get(originalMessage.getId()).editMessage(MessageEditData.fromMessage(originalMessage)).queue();
        } catch (Exception exception) {
            originalMessage.clearReactions().queue();
            originalMessage.addReaction(EmojiUtils.NO).queue();

            messages.remove(originalMessage.getId());
        }
    }

    @SubscribeEvent
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        @NotNull final String messageID = event.getMessageId();
        if(!messages.containsKey(messageID)) return;

        messages.remove(messageID);
        messages.get(messageID).delete().queue();
    }
}
