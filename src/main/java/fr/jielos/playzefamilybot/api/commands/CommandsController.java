package fr.jielos.playzefamilybot.api.commands;

import fr.jielos.playzefamilybot.PlayzeFamilyBot;
import fr.jielos.playzefamilybot.api.APIComponent;
import fr.jielos.playzefamilybot.api.events.EventsController;
import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.utils.common.ui.MessageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandsController extends APIComponent implements Controller<CommandsController> {

    @NotNull private final EventsController eventsController;

    @Getter @NotNull private final Map<String, List<CommandAdapter>> commands;

    public CommandsController(@NotNull PlayzeFamilyBot instance, @NotNull JDA client, @NotNull EventsController eventsController) {
        super(instance, client);

        this.commands = new HashMap<>();

        this.eventsController = eventsController;
    }

    @NotNull
    @Override
    public CommandsController load() {
        eventsController.registerEventAdapter(this);

        PlayzeFamilyBot.getLogger().info("Successful loading of Commands controller.");

        return this;
    }

    public void deleteUnregisteredCommands() {
        api.retrieveCommands().queue(commands -> {
            commands.stream().filter(command -> !this.commands.containsKey(command.getName())).forEach(command -> command.delete().queue());
        });
    }

    public void registerCommandAdapter(@NotNull CommandAdapter commandAdapter) {
        @NotNull final List<CommandData> commandsData = new ArrayList<>();

        if(commandAdapter instanceof @NotNull final SlashCommandAdapter slashCommandAdapter) commandsData.add(slashCommandAdapter.getSlashCommandData());
        if(commandAdapter instanceof @NotNull final MessageCommandAdapter messageCommandAdapter) commandsData.add(messageCommandAdapter.getMessageCommandData());
        if(commandAdapter instanceof @NotNull final UserCommandAdapter userCommandAdapter) commandsData.add(userCommandAdapter.getUserCommandData());

        for(@NotNull CommandData commandData : commandsData) {
            @NotNull final String commandName = commandData.getName();
            @NotNull final List<CommandAdapter> adapters = commands.getOrDefault(commandName, new ArrayList<>());

            if(adapters.isEmpty()) {
                api.upsertCommand(commandData).queue();
            }

            adapters.add(commandAdapter);

            commands.put(commandName, adapters);
        }

        eventsController.registerEventAdapter(commandAdapter);
    }

    @Override
    public void onGenericInteractionCreate(@NotNull GenericInteractionCreateEvent event) {
        try {
            if(!(event instanceof @NotNull final CommandInteractionPayload commandInteractionPayload)) return;

            @NotNull final String commandName = commandInteractionPayload.getName();
            @NotNull final List<CommandAdapter> commandAdapters = commands.getOrDefault(commandName, new ArrayList<>());

            for(@NotNull final CommandAdapter commandAdapter : commandAdapters) {
                if(commandAdapter instanceof @NotNull final GuildComponent guildComponent) {
                    if(!event.isFromGuild() || guildComponent.getGuild() != event.getGuild()) {
                        continue;
                    }
                }

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
            if(!(event instanceof @NotNull final IReplyCallback iReplyCallback)) return;

            iReplyCallback.reply(MessageUtils.error(exception)).setEphemeral(true).setSuppressEmbeds(true).queue();
        }

        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}