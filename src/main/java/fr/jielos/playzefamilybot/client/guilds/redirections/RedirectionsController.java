package fr.jielos.playzefamilybot.client.guilds.redirections;

import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.settings.GuildSetting;
import fr.jielos.playzefamilybot.client.guilds.settings.SettingsController;
import fr.jielos.playzefamilybot.utils.common.ResourceUtils;
import fr.jielos.playzefamilybot.utils.common.ui.EmojiUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RedirectionsController extends GuildComponent implements Controller<RedirectionsController> {

    @NotNull private final Map<String, Message> redirectedMessages;

    @NotNull private final SettingsController settingsController;

    public RedirectionsController(@NotNull GuildCache guildCache, @NotNull SettingsController settingsController) {
        super(guildCache);

        this.redirectedMessages = new HashMap<>();

        this.settingsController = settingsController;
    }

    @NotNull
    @Override
    public RedirectionsController load() {
        instance.getEventsController().registerEventAdapter(this);

        return Controller.super.load();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;

        @NotNull final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @NotNull final User author = event.getAuthor();
        if(author.isBot()) return;

        @NotNull final Channel channel = event.getChannel();
        @NotNull final Message message = event.getMessage();

        @Nullable final TextChannel receiverChannel = settingsController.retrieveSetting(GuildSetting.GUILD_BOT_SPEAKING_RECEIVER);
        if(receiverChannel == null || !channel.getId().equals(receiverChannel.getId())) return;

        @Nullable final TextChannel redirectChannel = settingsController.retrieveSetting(GuildSetting.GUILD_BOT_SPEAKING_TARGET);
        if(redirectChannel == null) return;

        redirectChannel.sendMessage(message.getContentRaw()).setFiles(ResourceUtils.retrieveAttachmentsFiles(message.getAttachments())).setStickers(message.getStickers()).queue(redirectedMessage -> {
            message.addReaction(EmojiUtils.EMOJI_YES).queue();

            redirectedMessages.put(message.getId(), redirectedMessage);
        }, error -> {
            message.addReaction(EmojiUtils.EMOJI_NO).queue();
        });
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        @NotNull final Message originalMessage = event.getMessage();
        if(!redirectedMessages.containsKey(originalMessage.getId())) return;

        try {
            redirectedMessages.get(originalMessage.getId()).editMessage(MessageEditData.fromMessage(originalMessage)).queue();
        } catch (Exception exception) {
            originalMessage.clearReactions().queue();
            originalMessage.addReaction(EmojiUtils.EMOJI_NO).queue();

            redirectedMessages.remove(originalMessage.getId());
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        @NotNull final String messageID = event.getMessageId();
        if(!redirectedMessages.containsKey(messageID)) return;

        redirectedMessages.remove(messageID);
        redirectedMessages.get(messageID).delete().queue();
    }
}
