package fr.jielos.playzefamilybot.api.commands;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public interface UserCommandAdapter extends CommandAdapter {

    @NotNull CommandData getUserCommandData();

    void onUserContext(@NotNull UserContextInteractionEvent event) throws Exception;

}
