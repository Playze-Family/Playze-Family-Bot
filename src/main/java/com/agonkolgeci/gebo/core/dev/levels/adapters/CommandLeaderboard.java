package com.agonkolgeci.gebo.core.dev.levels.adapters;

import com.agonkolgeci.gebo.core.commands.SlashCommandAdapter;
import com.agonkolgeci.gebo.core.dev.GuildCache;
import com.agonkolgeci.gebo.core.dev.GuildComponent;
import com.agonkolgeci.gebo.core.dev.levels.LevelsController;
import com.agonkolgeci.gebo.core.dev.members.profile.MemberProfileCache;
import com.agonkolgeci.gebo.utils.common.ui.ColorUtils;
import com.agonkolgeci.gebo.utils.common.ui.MessageUtils;
import com.agonkolgeci.gebo.utils.common.fonts.FontUtils;
import com.agonkolgeci.gebo.utils.common.images.ImageUtils;
import com.agonkolgeci.gebo.utils.graphics.DrawableImage;
import com.agonkolgeci.gebo.utils.graphics.shapes.Text;
import com.agonkolgeci.gebo.utils.graphics.shapes.builders.elements.ImageViewBuilder;
import com.agonkolgeci.gebo.utils.graphics.shapes.builders.elements.RectangleBuilder;
import com.agonkolgeci.gebo.utils.graphics.shapes.builders.elements.TextBuilder;
import com.agonkolgeci.gebo.utils.graphics.shapes.builders.positions.HPos;
import com.agonkolgeci.gebo.utils.graphics.shapes.builders.positions.VPos;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;

public class CommandLeaderboard extends GuildComponent implements SlashCommandAdapter {

    public static final int LEADERBOARD_CARD_IMAGE_WIDTH = 960;
    public static final int LEADERBOARD_CARD_IMAGE_HEIGHT = 1080;

    public static final int LEADERBOARD_CARD_PROFILES_PER_PAGE = 10;

    @NotNull private final LevelsController levelsController;

    public CommandLeaderboard(@NotNull GuildCache guildCache, @NotNull LevelsController levelsController) {
        super(guildCache);

        this.levelsController = levelsController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("leaderboard", "Visualiser le classement général du serveur.").setGuildOnly(true);
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception {
        @Nullable final Member member = event.getMember();
        if(member == null) return;

        @NotNull final List<MemberProfileCache> activeProfiles = levelsController.retrieveActiveProfiles();

        Checks.check(!member.getUser().isBot(), MessageUtils.ERROR_MEMBER_MUST_BE_HUMAN);

        event.deferReply().setEphemeral(true).queue(interactionHook -> {
            @NotNull final DrawableImage leaderboardCard = retrieveLeaderboardCard(activeProfiles);

            interactionHook.editOriginalAttachments(leaderboardCard.toFileUpload("leaderboard_card", ImageUtils.FILES_FORMAT, guild)).queue();
        });
    }

    @NotNull
    public static DrawableImage retrieveLeaderboardCard(@NotNull List<MemberProfileCache> memberProfiles) {
        @NotNull final DrawableImage drawableImage = new DrawableImage(LEADERBOARD_CARD_IMAGE_WIDTH, LEADERBOARD_CARD_IMAGE_HEIGHT);

        @NotNull final BufferedImage backgroundImage = drawableImage.drawBackground(ImageUtils.CURRENT_BACKGROUND);
        @NotNull final Color averageColor = ColorUtils.getAverageColor(backgroundImage);

        @NotNull final Stroke defaultStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

        @NotNull final RoundRectangle2D container = new RectangleBuilder(drawableImage, 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), 75, 75).transform(60, 90).translate(0, 30).color(ColorUtils.getColor(averageColor.brighter(), 0.5)).stroke(defaultStroke, averageColor.darker()).paint();
        @NotNull final RoundRectangle2D subContainer = new RectangleBuilder(drawableImage, container.getMinX(), container.getMinY(), container.getWidth(), container.getHeight()).transform(25, 25).build();

        @NotNull final Text title = new TextBuilder(drawableImage, "Classement des niveaux".toUpperCase(Locale.ROOT), FontUtils.ZABAL_EXTRA_BOLD.deriveFont(50F), HPos.CENTER, VPos.CENTER, container.getCenterX(), 60).color(Color.WHITE).paint();

        final int defaultPadding = 10;
        for(@NotNull MemberProfileCache memberProfileCache : memberProfiles.subList(0, Math.min(memberProfiles.size(), LEADERBOARD_CARD_PROFILES_PER_PAGE))) {
            final int profilePlace = memberProfiles.indexOf(memberProfileCache) + 1;

            final double profileWidth = subContainer.getWidth();
            final double profileHeight = (subContainer.getHeight() / LEADERBOARD_CARD_PROFILES_PER_PAGE);

            final double profileX = subContainer.getMinX();
            final double profileY = subContainer.getMinY() + ((profilePlace - 1) * profileHeight);

            @NotNull final RoundRectangle2D profileContainer = new RectangleBuilder(drawableImage, profileX, profileY, profileWidth, profileHeight, container.getArcWidth(), container.getArcHeight()).transform(defaultPadding, defaultPadding).build();

            @NotNull final RoundRectangle2D profilePosition = new RectangleBuilder(drawableImage, profileContainer.getMinX(), profileContainer.getMinY(), profileContainer.getHeight(), profileContainer.getHeight()).build();
            switch (profilePlace) {
                case 1, 2, 3 -> {
                    new ImageViewBuilder(drawableImage, switch (profilePlace) {
                        case 1 -> ImageUtils.ICON_TROPHY;
                        case 2 -> ImageUtils.ICON_SECOND_PLACE;
                        case 3 -> ImageUtils.ICON_THIRD_PLACE;

                        default -> ImageUtils.ICON_MEDAL;
                    }, profilePosition.getMinX(), profileContainer.getMinY(), profilePosition.getWidth(), profilePosition.getHeight()).transform(10, 10).paint();
                }

                default -> {
                    new TextBuilder(drawableImage, "#" + profilePlace, FontUtils.ZABAL_EXTRA_BOLD.deriveFont(25F), HPos.CENTER, VPos.CENTER, profilePosition.getCenterX(), profilePosition.getCenterY()).color(Color.WHITE).paint();
                }
            }

            @NotNull final RoundRectangle2D profileAvatar = new ImageViewBuilder(drawableImage, memberProfileCache.getMemberCache().getAvatar(), profilePosition.getMaxX(), profileContainer.getMinY(), profileContainer.getHeight(), profileContainer.getHeight(), profileContainer.getArcWidth(), profileContainer.getArcHeight()).translate(defaultPadding, 0).stroke(defaultStroke, averageColor.darker()).paint();

            @NotNull final RoundRectangle2D profileInfos = new RectangleBuilder(drawableImage, profileAvatar.getMaxX(), profileAvatar.getMinY(), profileContainer.getWidth() - (profileAvatar.getMaxX() - profilePosition.getMinX()), profileContainer.getHeight(), container.getArcWidth() / 2, container.getArcHeight() / 2).translate(defaultPadding * 2, 0).stroke(defaultStroke, averageColor.darker()).color(new Color(averageColor.getRed(), averageColor.getGreen(), averageColor.getBlue(), averageColor.getAlpha() / 2)).paint();
            @NotNull final RoundRectangle2D profileInfosContainer = new RectangleBuilder(drawableImage, profileInfos.getMinX(), profileInfos.getMinY(), profileInfos.getWidth(), profileInfos.getHeight()).transform(defaultPadding, 0).build();

            @NotNull final Text profileUsername = new TextBuilder(drawableImage, "@", FontUtils.ZABAL_EXTRA_BOLD.deriveFont(30F), HPos.LEFT, VPos.CENTER, profileInfosContainer.getMinX(), profileInfosContainer.getCenterY()).color(Color.WHITE).attachText(memberProfileCache.getMember().getUser().getName(), FontUtils.ZABAL_REGULAR.deriveFont(35F), VPos.CENTER, Color.WHITE, 0).paint();
            @NotNull final Text profileLevel = new TextBuilder(drawableImage, String.valueOf(memberProfileCache.getLevel()), FontUtils.ZABAL_EXTRA_BOLD.deriveFont(45F), HPos.RIGHT, VPos.CENTER, profileInfosContainer.getMaxX(), profileInfosContainer.getCenterY()).color(Color.WHITE).attachText("Niveau", FontUtils.ZABAL_REGULAR.deriveFont(35F), VPos.CENTER, Color.WHITE, defaultPadding).paint();
        }

        return drawableImage;
    }

}
