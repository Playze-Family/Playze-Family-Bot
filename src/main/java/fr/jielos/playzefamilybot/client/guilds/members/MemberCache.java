package fr.jielos.playzefamilybot.client.guilds.members;

import fr.jielos.playzefamilybot.api.cache.CacheDirectory;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.members.custom_role.MemberCustomRoleCache;
import fr.jielos.playzefamilybot.client.guilds.members.invites.MemberInvitesCache;
import fr.jielos.playzefamilybot.client.guilds.members.profile.MemberProfileCache;
import fr.jielos.playzefamilybot.client.users.UserCache;
import fr.jielos.playzefamilybot.utils.CacheUtils;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import fr.jielos.playzefamilybot.utils.common.images.ImageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class MemberCache extends GuildComponent implements CacheDirectory {

    @Getter @NotNull private final UserCache userCache;

    @Getter @NotNull private final String memberID;

    @Nullable private MemberProfileCache profileCache;
    @Nullable private MemberInvitesCache invitesCache;
    @Nullable private MemberCustomRoleCache customRoleCache;

    public MemberCache(@NotNull GuildCache guildCache, @NotNull UserCache userCache, @NotNull String memberID) {
        super(guildCache);

        this.userCache = userCache;

        this.memberID = memberID;
    }

    @NotNull
    public Member getMember() {
        return Objects.requireNonNull(guild.getMemberById(memberID), String.format("Unable to get Member: %s", memberID));
    }

    @Override
    @NotNull
    public File getCacheDirectory() {
        return CacheUtils.retrieveCacheDirectory(guildCache.getCacheDirectory(), false, "members", memberID);
    }

    @NotNull
    public BufferedImage getAvatar() {
        @NotNull final Member member = getMember();
        if(member.getAvatarId() == null) return userCache.getAvatar();

        @NotNull final ImageProxy avatarProxy = member.getEffectiveAvatar();
        @NotNull final String avatarID = Objects.requireNonNullElse(member.getAvatarId(), member.getDefaultAvatarId());

        @NotNull final File avatarFile = retrieveCacheFile(ObjectUtils.formatFile(avatarID, ImageUtils.FILES_FORMAT), false);
        @Nullable final BufferedImage avatar = ImageUtils.requireImageOrElseDownload(avatarFile, avatarProxy, 512);

        return Objects.requireNonNullElse(avatar, ImageUtils.EMPTY_IMAGE);
    }

    @NotNull
    public MemberProfileCache retrieveProfileCache() {
        return this.profileCache = Objects.requireNonNullElseGet(profileCache, () -> {
            try {
                return new MemberProfileCache(this);
            } catch (Exception exception) {
                throw new RuntimeException(String.format("Unable to retrieve custom role from member: %s", memberID), exception);
            }
        });
    }

    @NotNull
    public MemberInvitesCache retrieveInvitesCache() {
        return this.invitesCache = Objects.requireNonNullElseGet(invitesCache, () -> {
            try {
                return new MemberInvitesCache(this);
            } catch (Exception exception) {
                throw new RuntimeException(String.format("Unable to retrieve invites from member: %s", memberID), exception);
            }
        });
    }

    @NotNull
    public MemberCustomRoleCache retrieveCustomRoleCache() {
        return this.customRoleCache = Objects.requireNonNullElseGet(customRoleCache, () -> {
            try {
                return new MemberCustomRoleCache(this);
            } catch (Exception exception) {
                throw new RuntimeException(String.format("Unable to retrieve custom role from member: %s", memberID), exception);
            }
        });
    }
}