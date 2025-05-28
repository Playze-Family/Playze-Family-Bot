package com.agonkolgeci.gebo.core.commands;

import com.agonkolgeci.gebo.BotAdapter;
import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.core.commands.settings.CommandSettings;
import com.agonkolgeci.gebo.core.commands.utils.CommandStats;
import com.agonkolgeci.gebo.utils.common.ui.MessageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandsManager implements BotAdapter {

    @NotNull private final GeboBot instance;

    @Getter @NotNull private final Map<String, List<CommandAdapter>> commands;

    public CommandsManager(@NotNull GeboBot instance) {
        this.instance = instance;

        this.commands = new HashMap<>();
    }

    @Override
    public void load() {
        instance.getApi().addEventListener(this);

        this.registerAdapter(new CommandStats(instance));
        this.registerAdapter(new CommandSettings(instance));

        // remove old commands
        instance.getApi().retrieveCommands().queue(commands -> {
            commands.stream().filter(command -> !this.commands.containsKey(command.getName())).forEach(command -> command.delete().queue());
        });

        GeboBot.LOGGER.info("Commands are ready.");
    }

    public void registerAdapter(@NotNull CommandAdapter commandAdapter) {
        @NotNull final List<CommandData> commandsData = new ArrayList<>();

        if(commandAdapter instanceof @NotNull final SlashCommandAdapter slashCommandAdapter) commandsData.add(slashCommandAdapter.getSlashCommandData());
        if(commandAdapter instanceof @NotNull final MessageCommandAdapter messageCommandAdapter) commandsData.add(messageCommandAdapter.getMessageCommandData());
        if(commandAdapter instanceof @NotNull final UserCommandAdapter userCommandAdapter) commandsData.add(userCommandAdapter.getUserCommandData());

        for(@NotNull CommandData commandData : commandsData) {
            @NotNull final String commandName = commandData.getName();
            @NotNull final List<CommandAdapter> adapters = commands.getOrDefault(commandName, new ArrayList<>());

            if(adapters.isEmpty()) {
                instance.getApi().upsertCommand(commandData).queue();
            }

            adapters.add(commandAdapter);

            commands.put(commandName, adapters);
        }
    }

    @SubscribeEvent
    public void onGenericInteractionCreate(@NotNull GenericInteractionCreateEvent event) {
        try {
            if(!(event instanceof @NotNull final CommandInteractionPayload interactionPayload)) return;

            @NotNull final String commandName = interactionPayload.getName();
            @NotNull final List<CommandAdapter> commandAdapters = commands.getOrDefault(commandName, new ArrayList<>());

            for(@NotNull final CommandAdapter commandAdapter : commandAdapters) {
                if(event instanceof @NotNull final SlashCommandInteractionEvent calledEvent && commandAdapter instanceof @NotNull final SlashCommandAdapter slashCommandAdapter) {
                    slashCommandAdapter.onSlashCommandComplete(calledEvent);
                }

                if(event instanceof @NotNull final UserContextInteractionEvent calledEvent && commandAdapter instanceof @NotNull final UserCommandAdapter userCommandAdapter) {
                    userCommandAdapter.onUserContext(calledEvent);
                }

                if(event instanceof @NotNull final MessageContextInteractionEvent calledEvent && commandAdapter instanceof @NotNull final MessageCommandAdapter messageCommandAdapter) {
                    messageCommandAdapter.onMessageContext(calledEvent);
                }
            }
        }

        catch(ErrorResponseException ignored) {}

        catch (IllegalArgumentException | IllegalStateException exception) {
            if(!(event instanceof IReplyCallback replyCallback)) return;

            replyCallback.reply(MessageUtils.error(exception)).setEphemeral(true).setSuppressEmbeds(true).queue();
        }
    }

}