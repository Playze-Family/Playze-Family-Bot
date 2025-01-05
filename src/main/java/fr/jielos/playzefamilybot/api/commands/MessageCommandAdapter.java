package fr.jielos.playzefamilybot.api.commands;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public interface MessageCommandAdapter extends CommandAdapter {

    @NotNull CommandData getMessageCommandData();

    void onMessageContext(@NotNull MessageContextInteractionEvent event) throws Exception;

}
