package com.agonkolgeci.playze_family_bot.client.guilds;

import com.agonkolgeci.playze_family_bot.api.cache.CacheDirectory;
import com.agonkolgeci.playze_family_bot.client.ClientCache;
import com.agonkolgeci.playze_family_bot.client.ClientComponent;
import com.agonkolgeci.playze_family_bot.client.guilds.channels.ChannelsController;
import com.agonkolgeci.playze_family_bot.client.guilds.invites.InvitesController;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.LevelsController;
import com.agonkolgeci.playze_family_bot.client.guilds.members.MembersController;
import com.agonkolgeci.playze_family_bot.client.guilds.redirections.RedirectionsController;
import com.agonkolgeci.playze_family_bot.client.guilds.settings.SettingsController;
import com.agonkolgeci.playze_family_bot.utils.CacheUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
import com.agonkolgeci.playze_family_bot.utils.common.images.ImageUtils;
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

    @Getter @NotNull private final SettingsController settingsController;
    @Getter @NotNull private final MembersController membersController;

    @Getter @NotNull private final InvitesController invitesController;

    @Getter @NotNull private final LevelsController levelsController;
    @Getter @NotNull private final RedirectionsController redirectionsController;

    @Getter @NotNull private final ChannelsController autoChannelsController;

    public GuildCache(@NotNull ClientCache clientCache, @NotNull String guildID) {
        super(clientCache);

        this.guildID = guildID;

        this.settingsController = new SettingsController(this).load();

        this.membersController = new MembersController(this).load();

        this.invitesController = new InvitesController(this, settingsController, membersController).load();

        this.levelsController = new LevelsController(this, settingsController, membersController).load();
        this.redirectionsController = new RedirectionsController(this, settingsController).load();

        this.autoChannelsController = new ChannelsController(this, settingsController).load();
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
        return CacheUtils.retrieveCacheDirectory(false, "guilds", guildID);
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