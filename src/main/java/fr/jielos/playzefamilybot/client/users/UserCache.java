package fr.jielos.playzefamilybot.client.users;

import fr.jielos.playzefamilybot.api.cache.CacheDirectory;
import fr.jielos.playzefamilybot.client.ClientCache;
import fr.jielos.playzefamilybot.client.ClientComponent;
import fr.jielos.playzefamilybot.utils.CacheUtils;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import fr.jielos.playzefamilybot.utils.common.images.ImageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class UserCache extends ClientComponent implements CacheDirectory {

    @Getter @NotNull private final String userID;

    public UserCache(@NotNull ClientCache clientCache, @NotNull String userID) {
        super(clientCache);

        this.userID = userID;
    }

    @NotNull
    public User getUser() {
        return Objects.requireNonNull(clientCache.getApi().getUserById(userID), String.format("Unable to get User: %s", userID));
    }

    @Override
    @NotNull
    public File getCacheDirectory() {
        return CacheUtils.retrieveCacheDirectory(false, "users", userID);
    }

    @NotNull
    public BufferedImage getAvatar() {
        @NotNull final User user = getUser();

        @NotNull final String avatarID = Objects.requireNonNullElse(user.getAvatarId(), user.getDefaultAvatarId());
        @NotNull final ImageProxy avatarProxy = user.getEffectiveAvatar();

        @NotNull final File avatarFile = retrieveCacheFile(ObjectUtils.formatFile(avatarID, ImageUtils.FILES_FORMAT), false);
        @Nullable final BufferedImage avatar = ImageUtils.requireImageOrElseDownload(avatarFile, avatarProxy, 512);

        return Objects.requireNonNullElse(avatar, ImageUtils.EMPTY_IMAGE);
    }
}
