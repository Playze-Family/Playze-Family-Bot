package com.agonkolgeci.gebo.core.dev;

import com.agonkolgeci.gebo.api.cache.CacheDirectory;
import com.agonkolgeci.gebo.core.ClientCache;
import com.agonkolgeci.gebo.core.ClientComponent;
import com.agonkolgeci.gebo.core.channels.ChannelsController;
import com.agonkolgeci.gebo.core.dev.invites.InvitesController;
import com.agonkolgeci.gebo.core.dev.levels.LevelsController;
import com.agonkolgeci.gebo.core.dev.members.MembersController;
import com.agonkolgeci.gebo.core.dev.redirections.RedirectionsController;
import com.agonkolgeci.gebo.core.settings.SettingsManager;
import com.agonkolgeci.gebo.utils.CacheUtils;
import com.agonkolgeci.gebo.utils.common.ObjectUtils;
import com.agonkolgeci.gebo.utils.common.images.ImageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class GuildCache extends ClientComponent implements CacheDirectory {

    @Getter @NotNull private final String guildID;

    @Getter @NotNull private final SettingsManager settingsManager;
    @Getter @NotNull private final MembersController membersController;

    @Getter @NotNull private final InvitesController invitesController;

    @Getter @NotNull private final LevelsController levelsController;
    @Getter @NotNull private final RedirectionsController redirectionsController;

    @Getter @NotNull private final ChannelsController autoChannelsController;

    public GuildCache(@NotNull ClientCache clientCache, @NotNull String guildID) {
        super(clientCache);

        this.guildID = guildID;

        this.settingsManager = new SettingsManager(this).load();

        this.membersController = new MembersController(this).load();

        this.invitesController = new InvitesController(this, settingsManager, membersController).load();

        this.levelsController = new LevelsController(this, settingsManager, membersController).load();
        this.redirectionsController = new RedirectionsController(this, settingsManager).load();

        this.autoChannelsController = new ChannelsController(this, settingsManager).load();
    }

    @NotNull
    public Guild getGuild() {
        return Objects.requireNonNull(clientCache.getApi().getGuildById(guildID), String.format("Unable to get Guild: %s", guildID));
    }

    @NotNull
    public Member getSelfMember() {
        return getGuild().getSelfMember();
    }

    @Override
    @NotNull
    public File getCacheDirectory() {
        return CacheUtils.retrieveCacheDirectory(false, "dev", guildID);
    }

    @NotNull
    public BufferedImage getIcon() {
        @NotNull final Guild guild = getGuild();

        @Nullable final ImageProxy iconProxy = guild.getIcon();
        @Nullable final String iconID = guild.getIconId();
        if(iconProxy == null || iconID == null) return ImageUtils.EMPTY_IMAGE;

        @NotNull final File iconFile = retrieveCacheFile(ObjectUtils.formatFile(iconID, ImageUtils.FILES_FORMAT), false);
        @Nullable final BufferedImage icon = ImageUtils.requireImageOrElseDownload(iconFile, iconProxy, 512);

        return Objects.requireNonNullElse(icon, ImageUtils.EMPTY_IMAGE);
    }

}