package com.agonkolgeci.gebo.core.dev.members.profile;

import com.agonkolgeci.gebo.api.cache.CacheConnector;
import com.agonkolgeci.gebo.api.events.member.GenericGuildMemberProfileUpdate;
import com.agonkolgeci.gebo.core.dev.levels.adapters.CommandLevel;
import com.agonkolgeci.gebo.core.dev.members.MemberCache;
import com.agonkolgeci.gebo.core.dev.members.MemberComponent;
import com.agonkolgeci.gebo.core.dev.members.profile.level.MemberProfileLevel;
import com.agonkolgeci.gebo.core.dev.members.profile.voice.MemberVoiceState;
import com.agonkolgeci.gebo.core.dev.members.profile.voice.MemberVoiceTimer;
import com.agonkolgeci.gebo.core.settings.GuildSettingLegacy;
import com.agonkolgeci.gebo.utils.common.ObjectUtils;
import com.agonkolgeci.gebo.utils.common.images.ImageUtils;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MemberProfileCache extends MemberComponent implements CacheConnector {

    @Getter private int level;
    @Getter private int xp;
    @Getter private int timeSpentInVoice;
    @Getter private int totalMessages;
    @Getter private int totalReactions;

    @Nullable private BufferedImage customBackground;

    @Nullable private MemberVoiceTimer memberVoiceTimer;

    public MemberProfileCache(@NotNull MemberCache memberCache) {
        super(memberCache);

        databaseManager.executeQuery("SELECT * FROM guilds_members_profiles WHERE guild_id = ? AND member_id = ?", profiles -> {
            if(profiles.next()) {
                this.level = profiles.getInt("level");
                this.xp = profiles.getInt("xp");
                this.timeSpentInVoice = profiles.getInt("time_spent_in_voice");
                this.totalMessages = profiles.getInt("total_messages");
                this.totalReactions = profiles.getInt("total_reactions");
            }
        }, guild.getId(), member.getId());
    }

    public void setProperties(int level, int xp) {
        Checks.check(level >= 0, "La valeur du niveau du membre doit être positif.");
        Checks.check(xp >= 0, "La valeur de l'expérience du membre doit être positif.");
        Checks.check(level < Integer.MAX_VALUE && xp < Integer.MAX_VALUE, "La valeur du niveau/points d'expériences du membre ne doit pas excéder %d.", Integer.MAX_VALUE);

        final double completeXP = MemberProfileLevel.retrieveCompleteXP(level);
        Checks.check(xp < completeXP, "La valeur de l'expérience du membre doit être strictement inférieur à la valeur maximum d'expérience du niveau renseigné (%f max XP pour le niveau %d).", completeXP, level);

        instance.getEventsController().handleEvent(new GenericGuildMemberProfileUpdate(api, member, this.level, this.xp, level, xp));

        this.level = level;
        this.xp = xp;

        this.saveCache();
    }

    public void addXP(int level, int xp) {
        int finalLevel = this.level + level;
        int finalXP = this.xp + xp;

        while(finalXP >= MemberProfileLevel.retrieveCompleteXP(finalLevel)) {
            finalXP -= MemberProfileLevel.retrieveCompleteXP(finalLevel);
            finalLevel++;
        }

        if(this.level == finalLevel && this.xp == finalXP) return;

        this.setProperties(finalLevel, finalXP);
    }

    public void subtractXP(int level, int xp) {
        int finalLevel = this.level - level;
        int finalXP = this.xp - xp;

        while(finalXP < 0) {
            finalXP += MemberProfileLevel.retrieveCompleteXP(finalLevel-1);
            finalLevel--;
        }

        this.setProperties(finalLevel, finalXP);
    }

    public void increaseTimeSpentInVoice(int timeSpentInVoice) {
        this.timeSpentInVoice += timeSpentInVoice;

        this.saveCache();
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;

        this.saveCache();
    }

    public void setTotalReactions(int totalReactions) {
        this.totalReactions = totalReactions;

        this.saveCache();
    }

    public int getCompleteXP() {
        return MemberProfileLevel.retrieveCompleteXP(level);
    }

    public int getTotalXP() {
        return MemberProfileLevel.retrieveTotalXP(level, xp);
    }

    public void attributeXP(@NotNull MemberProfileLevel memberProfileLevel) {
        addXP(0, memberProfileLevel.getGain() * (int) guildCache.getSettingsManager().retrieveSetting(GuildSettingLegacy.GUILD_MULTIPLIER_XP_GAIN));

        switch (memberProfileLevel) {
            case MESSAGE -> setTotalMessages(totalMessages+1);
            case REACTION -> setTotalReactions(totalReactions+1);
        }
    }

    public void takeXP(@NotNull MemberProfileLevel memberProfileLevel) {
        if(xp <= 0) return;

        subtractXP(0, memberProfileLevel.getGain() * (int) guildCache.getSettingsManager().retrieveSetting(GuildSettingLegacy.GUILD_MULTIPLIER_XP_GAIN));

        switch (memberProfileLevel) {
            case MESSAGE -> setTotalMessages(totalMessages-1);
            case REACTION -> setTotalReactions(totalReactions-1);
        }
    }

    public boolean isBackgroundUnlocked() {
        if(member.hasPermission(Permission.ADMINISTRATOR)) return true;

        @Nullable final Role customRole = guildCache.getSettingsManager().retrieveSetting(GuildSettingLegacy.GUILD_MEMBERS_CUSTOM_ROLE);
        return customRole != null && member.getRoles().contains(customRole);
    }

    @NotNull
    public File getBackgroundFile() {
        return memberCache.retrieveCacheFile(ObjectUtils.formatFile("profile_background", ImageUtils.FILES_FORMAT), false);
    }

    public void updateBackgroundImage(@NotNull ImageProxy imageProxy) {
        @NotNull final File backgroundFile = getBackgroundFile();
        @Nullable final BufferedImage newBackground = ImageUtils.updateImage(backgroundFile, imageProxy);

        Checks.check(newBackground != null, "Une erreur s'est produite lors de la mise à jour de votre nouveau arrière-plan.");
        Checks.check(newBackground.getWidth() >= CommandLevel.LEVEL_CARD_IMAGE_WIDTH && newBackground.getHeight() >= CommandLevel.LEVEL_CARD_IMAGE_HEIGHT, "Votre arrière-plan ne respecte pas les dimensions adéquates (minimum: %dx%d).",  CommandLevel.LEVEL_CARD_IMAGE_WIDTH,  CommandLevel.LEVEL_CARD_IMAGE_HEIGHT);

        this.customBackground = newBackground;
    }

    @NotNull
    public BufferedImage getBackgroundImage() {
        @NotNull final File backgroundFile = getBackgroundFile();

        return Objects.requireNonNullElseGet(customBackground, () -> {
            try {
                return this.customBackground = ImageIO.read(backgroundFile);
            } catch (Exception exception) {
                return ImageUtils.CURRENT_BACKGROUND;
            }
        });
    }

    public void initVoiceTimer() {
        if(memberVoiceTimer != null) this.deleteVoiceInterval();

        this.memberVoiceTimer = new MemberVoiceTimer(this);
    }

    public void deleteVoiceInterval() {
        if(memberVoiceTimer != null) {
            this.memberVoiceTimer.delete();
            this.memberVoiceTimer = null;
        }
    }

    public void checkVoiceState() {
        if(isConnected()) {
            if(memberVoiceTimer == null) {
                this.initVoiceTimer();
            }
        } else if(memberVoiceTimer != null) {
            this.deleteVoiceInterval();
        }
    }
    
    public boolean isConnected() {
        @Nullable final GuildVoiceState guildVoiceState = member.getVoiceState();
        return guildVoiceState != null && guildVoiceState.inAudioChannel();
    }

    public boolean isAlive() {
        return getVoiceState() == MemberVoiceState.ALIVE;
    }

    @NotNull
    public MemberVoiceState getVoiceState() {
        @Nullable final GuildVoiceState guildVoiceState = member.getVoiceState();

        if(guildVoiceState != null && guildVoiceState.inAudioChannel() && memberVoiceTimer != null) {
            if(guildVoiceState.getChannel() == member.getGuild().getAfkChannel()) return MemberVoiceState.IDLE;
            if(guildVoiceState.isSelfDeafened()) return MemberVoiceState.SELF_DEAFENED;

            return MemberVoiceState.ALIVE;
        }

        return MemberVoiceState.DISCONNECTED;
    }

    @Override
    public void saveCache() {
        CompletableFuture.runAsync(() -> {
            databaseManager.executeUpdate(
                "INSERT INTO guilds_members_profiles(guild_id, member_id, level, xp, time_spent_in_voice, total_messages, total_reactions) VALUES(?, ?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE level = VALUES(level), xp = VALUES(xp), time_spent_in_voice = VALUES(time_spent_in_voice), total_messages = VALUES(total_messages), total_reactions = VALUES(total_reactions)",
                guild.getId(), member.getId(),
                level, xp,
                timeSpentInVoice, totalMessages, totalReactions
            );
        });
    }

}