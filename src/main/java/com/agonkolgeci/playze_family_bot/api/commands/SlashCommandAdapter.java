package com.agonkolgeci.playze_family_bot.api.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public interface SlashCommandAdapter extends CommandAdapter {

    @NotNull CommandData getSlashCommandData();

    void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception;

}
