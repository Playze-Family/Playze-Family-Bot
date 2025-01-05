package fr.jielos.playzefamilybot.utils.common.images;

import fr.jielos.playzefamilybot.PlayzeFamilyBot;
import fr.jielos.playzefamilybot.utils.common.ResourceUtils;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("all")
public class ImageUtils {

    @NotNull public static final List<String> ACCEPTED_FILE_EXTENSIONS = Arrays.asList(ImageIO.getWriterFormatNames());
    @NotNull public static final String FILES_FORMAT = "png";

    @NotNull public static final BufferedImage EMPTY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    @NotNull private static final String ROOT_PATH = "images";

    @NotNull public static final BufferedImage DEFAULT_BACKGROUND = retrieveLocalImage("backgrounds", "default_background.png");
    @NotNull public static final BufferedImage YELLOW_BACKGROUND = retrieveLocalImage("backgrounds", "yellow_background.png");

    @NotNull public static final BufferedImage STRANGER_THINGS_BACKGROUND = retrieveLocalImage("backgrounds", "seasons", "stranger_things_background.png");
    @NotNull public static final BufferedImage STAR_WARS_BACKGROUND = retrieveLocalImage("backgrounds", "seasons", "star_wars_background.png");

    @NotNull public static final BufferedImage CURRENT_BACKGROUND = DEFAULT_BACKGROUND;

    @NotNull public static final BufferedImage ICON_TROPHY = retrieveLocalImage("icons", "leaderboard", "trophy.png");
    @NotNull public static final BufferedImage ICON_SECOND_PLACE = retrieveLocalImage("icons", "leaderboard", "second_place.png");
    @NotNull public static final BufferedImage ICON_THIRD_PLACE = retrieveLocalImage("icons", "leaderboard", "third_place.png");

    @NotNull public static final BufferedImage ICON_MICROPHONE = retrieveLocalImage("icons", "profiles", "microphone.png");
    @NotNull public static final BufferedImage ICON_MESSAGES = retrieveLocalImage("icons", "profiles", "messages.png");
    @NotNull public static final BufferedImage ICON_REACTIONS = retrieveLocalImage("icons", "profiles", "reactions.png");
    @NotNull public static final BufferedImage ICON_MEDAL = retrieveLocalImage("icons", "profiles", "medal.png");

    @NotNull public static final BufferedImage ICON_ONLINE = retrieveLocalImage("icons", "status", "online.png");
    @NotNull public static final BufferedImage ICON_OFFLINE = retrieveLocalImage("icons", "status", "offline.png");
    @NotNull public static final BufferedImage ICON_IDLE = retrieveLocalImage("icons", "status", "idle.png");
    @NotNull public static final BufferedImage ICON_SELF_DEAFENED = retrieveLocalImage("icons", "status", "self_deafened.png");

    @NotNull
    private static BufferedImage retrieveLocalImage(@NotNull String... objects) {
        try {
            return ImageIO.read(ResourceUtils.retrieveLocalInputStream(ROOT_PATH, objects));
        } catch (Exception exception) {
            PlayzeFamilyBot.getLogger().warn("Unable to read image: " + Path.of(ROOT_PATH, objects).toString());
        }

        return EMPTY_IMAGE;
    }

    @NotNull
    public static InputStream toInputStream(@NotNull BufferedImage bufferedImage, @NotNull String formatName) {
        @NotNull final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(bufferedImage, formatName, byteArrayOutputStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    @Nullable
    public static BufferedImage updateImage(@NotNull File target, @NotNull ImageProxy imageProxy) {
        try {
            return ImageIO.read(imageProxy.downloadToFile(target).get());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static BufferedImage requireImageOrElseDownload(@NotNull File target, @NotNull ImageProxy imageProxy) {
        try {
            return ImageIO.read(target.exists() ? target : imageProxy.downloadToFile(target).get());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static BufferedImage requireImageOrElseDownload(@NotNull File target, @NotNull ImageProxy imageProxy, int size) {
        try {
            return ImageIO.read(target.exists() ? target : imageProxy.downloadToFile(target, size).get());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

}

