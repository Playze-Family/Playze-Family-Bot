package fr.jielos.playzefamilybot.client.guilds.members.invites;

import fr.jielos.playzefamilybot.api.cache.CacheConnector;
import fr.jielos.playzefamilybot.client.guilds.invites.GuildInvite;
import fr.jielos.playzefamilybot.client.guilds.members.MemberCache;
import fr.jielos.playzefamilybot.client.guilds.members.MemberComponent;
import fr.jielos.playzefamilybot.client.guilds.members.profile.level.MemberProfileLevel;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MemberInvitesCache extends MemberComponent implements CacheConnector {

    @Getter private int total = 0;
    @Getter private int lefts = 0;

    @Getter @NotNull private final Map<Member, GuildInvite> history;

    public MemberInvitesCache(@NotNull MemberCache memberCache) {
        super(memberCache);

        this.history = new HashMap<>();

        databaseController.executeQuery("SELECT * FROM guilds_members_invites WHERE guild_id = ? AND member_id = ?", invites -> {
            if(invites.next()) {
                this.total = invites.getInt("total");
                this.lefts = invites.getInt("lefts");
            }
        }, guild.getId(), member.getId());

        databaseController.executeQuery("SELECT * FROM guilds_members_invites_history WHERE guild_id = ? AND member_id = ?", history -> {
             while(history.next()) {
                @Nullable final Member invitedMember = guild.getMemberById(history.getString("invited_id"));
                if(invitedMember == null) continue;

                @NotNull final Date invitedDate = history.getTimestamp("invited_date");
                if(this.history.containsKey(invitedMember)) {
                    if(this.history.get(invitedMember).getDate().before(invitedDate)) {
                        continue;
                    }
                }

                @NotNull final String invitationCode = history.getString("invitation_code");
                this.history.put(invitedMember, new GuildInvite(invitedDate, invitationCode));
            }
        }, guild.getId(), member.getId());

    }

    @NotNull
    public List<Invite> getInvites() {
        return guildCache.getInvitesController().getInvites().stream().filter(invite -> invite.getInviter() != null && invite.getInviter().getId().equals(member.getId())).toList();
    }

    public int getTrues() {
        return total - lefts;
    }

    public void increaseTotal(@NotNull Member invitedMember, @NotNull String invitationCode, int value) {
        this.total+=value;

        this.addInvited(invitedMember, invitationCode);
        this.saveCache();

        memberCache.retrieveProfileCache().attributeXP(MemberProfileLevel.INVITE);
    }

    public void increaseLeft(int value) {
        this.lefts += value;

        this.saveCache();
    }

    public void addInvited(@NotNull Member invitedMember, @NotNull String invitationCode) {
        @NotNull final Date invitedDate = new Date();

        CompletableFuture.runAsync(() -> {
            databaseController.executeUpdate(
                "INSERT INTO guilds_members_invites_history(guild_id, member_id, invited_id, invited_date, invitation_code) VALUES(?, ?, ?, ?, ?)",
                guild.getId(), member.getId(),
                invitedMember.getId(), new Timestamp(invitedDate.getTime()), invitationCode
            );
        });

        history.put(invitedMember, new GuildInvite(invitedDate, invitationCode));
    }

    @Override
    public void saveCache() {
        CompletableFuture.runAsync(() -> {
            databaseController.executeUpdate(
                "INSERT INTO guilds_members_invites(guild_id, member_id, total, lefts) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE total = VALUES(total), lefts = VALUES(lefts)",
                guild.getId(), member.getId(),
                total, lefts
            );
        });
    }

}
