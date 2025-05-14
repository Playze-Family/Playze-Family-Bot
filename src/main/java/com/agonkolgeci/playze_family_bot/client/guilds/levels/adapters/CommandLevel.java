package com.agonkolgeci.playze_family_bot.client.guilds.levels.adapters;

import com.agonkolgeci.playze_family_bot.api.commands.SlashCommandAdapter;
import com.agonkolgeci.playze_family_bot.api.messages.PEmbedBuilder;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildCache;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildComponent;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.LevelsController;
import com.agonkolgeci.playze_family_bot.client.guilds.members.MemberCache;
import com.agonkolgeci.playze_family_bot.client.guilds.members.profile.MemberProfileCache;
import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ResourceUtils;
import com.agonkolgeci.playze_family_bot.utils.common.fonts.FontUtils;
import com.agonkolgeci.playze_family_bot.utils.common.images.ImageUtils;
import com.agonkolgeci.playze_family_bot.utils.common.objects.IntegerUtils;
import com.agonkolgeci.playze_family_bot.utils.common.objects.TimeUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ui.ColorUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ui.EmojiUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ui.MessageUtils;
import com.agonkolgeci.playze_family_bot.utils.graphics.DrawableImage;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.Text;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.elements.ImageViewBuilder;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.elements.ProgressBarBuilder;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.elements.RectangleBuilder;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.elements.TextBuilder;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.positions.HPos;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.positions.VPos;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandLevel extends GuildComponent implements SlashCommandAdapter {

    public static final int LEVEL_CARD_IMAGE_WIDTH = 1200;
    public static final int LEVEL_CARD_IMAGE_HEIGHT = 400;

    @NotNull private final LevelsController levelsController;

    public CommandLevel(@NotNull GuildCache guildCache, @NotNull LevelsController levelsController) {
        super(guildCache);

        this.levelsController = levelsController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("level", "Gérer son niveau.")
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("view", "Afficher la progression de son niveau sur le serveur.").addOptions(
                                new OptionData(OptionType.USER, "membre", "Afficher la progression du niveau d'un membre spécifique", false)
                        ),
                        new SubcommandData("edit", "Éditer son profil de niveau.").addOptions(
                                new OptionData(OptionType.ATTACHMENT, "arrière-plan", String.format("Customiser l'arrière-plan de son profil de niveau (Taille minimale: %dpx * %dpx)", CommandLevel.LEVEL_CARD_IMAGE_WIDTH, CommandLevel.LEVEL_CARD_IMAGE_HEIGHT), true)
                        ),
                        new SubcommandData("rewards", "Visualiser les paliers de récompenses disponibles sur le serveur.")
                );
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final String subCommandName = event.getSubcommandName();
        if(subCommandName == null) return;

        switch (subCommandName) {
            case "view", "edit" -> {
                @Nullable final OptionMapping optionMember = event.getOption("membre");
                @Nullable final Member member = optionMember == null ? event.getMember() : optionMember.getAsMember();
                if(member == null) return;

                switch (subCommandName) {
                    case "view" -> {
                        @NotNull final MemberCache memberCache = guildCache.getMembersController().retrieveMemberCache(member);
                        @NotNull final MemberProfileCache memberProfileCache = memberCache.retrieveProfileCache();

                        Checks.check(!member.getUser().isBot(), MessageUtils.MEMBER_MUST_BE_HUMAN);

                        event.deferReply().setEphemeral(true).queue(interactionHook -> {
                             @NotNull final DrawableImage levelCard = retrieveLevelCard(memberProfileCache);

                            interactionHook.editOriginalAttachments(levelCard.toFileUpload("level_card", ImageUtils.FILES_FORMAT, guild, member)).queue();
                        });
                    }

                    case "edit" -> {
                        @NotNull final MemberCache memberCache = guildCache.getMembersController().retrieveMemberCache(member);
                        @NotNull final MemberProfileCache memberProfileCache = memberCache.retrieveProfileCache();

                        Checks.check(memberProfileCache.isBackgroundUnlocked(), "Vous n'avez pas débloquer l'arrière-plan du profil de niveau !");

                        @NotNull final Message.Attachment attachment = Objects.requireNonNull(event.getOption("arrière-plan")).getAsAttachment();

                        Checks.check(ImageUtils.ACCEPTED_FILE_EXTENSIONS.contains(attachment.getFileExtension()), "Ce type de fichier n'est pas pris en charge, vous devez fournir une image.");
                        Checks.check(attachment.getSize() <= ResourceUtils.MAX_SIZE_SUPPORTED, "Nous ne prenons pas en charge les fichiers dépassant %d Mo.", (ResourceUtils.MAX_SIZE_SUPPORTED / 1e+6));

                        event.deferReply().queue(interactionHook -> {
                            memberProfileCache.updateBackgroundImage(new ImageProxy(attachment.getUrl()));

                            interactionHook.editOriginal(MessageUtils.success("Votre arrière-plan du profil de niveau vient d'être édité !")).queue();
                        });
                    }
                }
            }

            case "rewards" -> {
                @NotNull final Map<Integer, List<Role>> rewards = levelsController.getRewards();
                Checks.check(!rewards.isEmpty(), "Aucune récompenses n'est configuré sur ce serveur.");

                @NotNull final PEmbedBuilder embedBuilder = new PEmbedBuilder(guild, "Paliers de récompenses", EmojiUtils.EMOJI_RIBBON, Color.CYAN);

                rewards.forEach((level, roles) -> {
                    embedBuilder.addField(String.format("Niveau %d \uD83C\uDF7B", level), roles.stream().map(role -> "• "+role.getAsMention()).collect(Collectors.joining("\n")), true);
                });

                event.replyEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    @NotNull
    public static DrawableImage retrieveLevelCard(@NotNull MemberProfileCache memberProfileCache) {
        @NotNull final DrawableImage drawableImage = new DrawableImage(LEVEL_CARD_IMAGE_WIDTH, LEVEL_CARD_IMAGE_HEIGHT);
        @NotNull final BufferedImage backgroundImage = drawableImage.drawBackground(memberProfileCache.getBackgroundImage());

        @NotNull final Color averageColor = ColorUtils.getAverageColor(backgroundImage);
        @NotNull final Color themeColor = ColorUtils.getThemeColor(averageColor);

        @NotNull final Stroke defaultStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

        @NotNull final RoundRectangle2D container = new RectangleBuilder(drawableImage, 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), 75, 75).transform(60, 60).color(ColorUtils.getColor(averageColor.brighter(), 0.5)).stroke(defaultStroke, averageColor.darker()).paint();
        @NotNull final RoundRectangle2D avatar = new ImageViewBuilder(drawableImage, memberProfileCache.getMemberCache().getAvatar(), container.getMinX(), container.getMinY(), container.getHeight(), container.getHeight(), container.getArcWidth(), container.getArcHeight()).stroke(defaultStroke, averageColor.darker()).paint();

        @NotNull final RoundRectangle2D subContainer = new RectangleBuilder(drawableImage, avatar.getMaxX(), container.getMinY(), container.getWidth() - avatar.getWidth(), container.getHeight()).transform(25, 25).build();

        @NotNull final Text displayName = new TextBuilder(drawableImage, ObjectUtils.cutString(memberProfileCache.getMember().getEffectiveName(), 15), FontUtils.ZABAL_EXTRA_BOLD.deriveFont(40F), HPos.LEFT, VPos.TOP, subContainer.getMinX(), subContainer.getMinY()).color(themeColor).paint();
        @NotNull final Text username = new TextBuilder(drawableImage, "@", FontUtils.ZABAL_EXTRA_BOLD.deriveFont(displayName.getFontSize()/1.25F), HPos.LEFT, VPos.TOP, displayName.getMinX(), displayName.getMaxY()).color(themeColor).translate(0, 12).attachText(memberProfileCache.getMember().getUser().getName(), FontUtils.ZABAL_REGULAR.deriveFont(displayName.getFontSize()/1.25F), VPos.CENTER, themeColor, 0).paint();

        @NotNull final Text level = new TextBuilder(drawableImage, String.valueOf(memberProfileCache.getLevel()), FontUtils.ZABAL_EXTRA_BOLD.deriveFont(60F), HPos.RIGHT, VPos.TOP, subContainer.getMaxX(), subContainer.getMinY()).color(themeColor).attachText("Niveau", FontUtils.ZABAL_REGULAR.deriveFont(60F-20F), VPos.CENTER, themeColor, 5).paint();
        @NotNull final Text totalXP = new TextBuilder(drawableImage, "XP Total", FontUtils.ZABAL_REGULAR.deriveFont(45F*0.65F), HPos.RIGHT, VPos.TOP, subContainer.getMaxX(), level.getMaxY()).translate(0, 30).color(averageColor.brighter().brighter().brighter().brighter().brighter().brighter()).attachText(IntegerUtils.formatNumber(memberProfileCache.getTotalXP(), 1), FontUtils.ZABAL_REGULAR.deriveFont(45F), VPos.BOTTOM, themeColor, 15).paint();

        @NotNull final Text totalVoiceTime = new TextBuilder(drawableImage, memberProfileCache.getTimeSpentInVoice() > 0 ? TimeUtils.formatDuration(memberProfileCache.getTimeSpentInVoice(), 2) : "Aucune activité", FontUtils.ZABAL_REGULAR.deriveFont(28F), HPos.LEFT, VPos.TOP, subContainer.getMinX(), username.getMaxY()).translate(0, 25).color(themeColor).attachImage(ImageUtils.ICON_MICROPHONE, HPos.LEFT, 28, 28, 10).paint();
        @NotNull final Text totalMessages = new TextBuilder(drawableImage, memberProfileCache.getTotalMessages() > 0 ? String.format("%,d message(s)", memberProfileCache.getTotalMessages()) : "Aucun message", FontUtils.ZABAL_REGULAR.deriveFont(28F), HPos.LEFT, VPos.TOP, subContainer.getMinX(), totalVoiceTime.getMaxY()).translate(0, 25).color(themeColor).attachImage(ImageUtils.ICON_MESSAGES, HPos.LEFT, 28, 28, 10).paint();
        @NotNull final Text totalReactions = new TextBuilder(drawableImage, memberProfileCache.getTotalReactions() > 0 ? String.format("%,d réaction(s)", memberProfileCache.getTotalReactions()) : "Aucune réaction", FontUtils.ZABAL_REGULAR.deriveFont(28F), HPos.LEFT, VPos.TOP, subContainer.getMinX(), totalMessages.getMaxY()).translate(0, 25).color(themeColor).attachImage(ImageUtils.ICON_REACTIONS, HPos.LEFT, 28, 28, 10).paint();

        @NotNull final RoundRectangle2D progressBar = new ProgressBarBuilder(drawableImage, subContainer.getMinX(), container.getMaxY() - 30.0 / 2, subContainer.getWidth(), 30, 25, 25).color(averageColor).complete(averageColor.brighter().brighter().brighter().brighter().brighter(), memberProfileCache.getXp(), memberProfileCache.getCompleteXP()).stroke(defaultStroke, averageColor.darker().darker()).paint();
        @NotNull final Text progression = new TextBuilder(drawableImage, String.format("%s / %s XP", IntegerUtils.formatNumber(memberProfileCache.getXp(), 1), IntegerUtils.formatNumber(memberProfileCache.getCompleteXP(), 1)), FontUtils.ZABAL_REGULAR.deriveFont((float)progressBar.getHeight()-10), HPos.LEFT, VPos.TOP, progressBar.getMinX(), progressBar.getMinY()).translate(12.5, 7.5).color(themeColor).paint();

        return drawableImage;
    }

}
