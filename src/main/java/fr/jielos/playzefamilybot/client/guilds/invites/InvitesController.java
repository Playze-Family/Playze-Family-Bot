package fr.jielos.playzefamilybot.client.guilds.invites;

import fr.jielos.playzefamilybot.api.managers.Controller;
import fr.jielos.playzefamilybot.api.managers.Permissionable;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.members.MemberCache;
import fr.jielos.playzefamilybot.client.guilds.members.MembersController;
import fr.jielos.playzefamilybot.client.guilds.members.invites.MemberInvitesCache;
import fr.jielos.playzefamilybot.client.guilds.members.profile.level.MemberProfileLevel;
import fr.jielos.playzefamilybot.client.guilds.settings.GuildSetting;
import fr.jielos.playzefamilybot.client.guilds.settings.SettingsController;
import fr.jielos.playzefamilybot.utils.common.objects.IntegerUtils;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class InvitesController extends GuildComponent implements Controller<InvitesController>, Permissionable {

    @NotNull @Getter  private List<Invite> invites;

    @NotNull private final SettingsController settingsController;
    @NotNull private final MembersController membersController;

    public InvitesController(@NotNull GuildCache guildCache, @NotNull SettingsController settingsController, @NotNull MembersController membersController) {
        super(guildCache);

        this.invites = hasPermissions() ? guild.retrieveInvites().complete() : new ArrayList<>();

        this.settingsController = settingsController;
        this.membersController = membersController;
    }

    @NotNull
    @Override
    public InvitesController load() {
        instance.getEventsController().registerEventAdapter(this);

        return Controller.super.load();
    }

    @Override
    public boolean hasPermissions() {
        return selfMember.hasPermission(Permission.MANAGE_SERVER);
    }

    @Nullable
    public Invite retrieveLastInviteUsed(@NotNull List<Invite> oldInvites, @NotNull List<Invite> newInvites) {
        return newInvites.stream().filter(oldInvites::contains).filter(anNewInvite -> {
            final int oldUses = oldInvites.stream().filter(anOldInvite -> anOldInvite.getCode().equals(anNewInvite.getCode())).map(Invite::getUses).findAny().orElse(0);
            return anNewInvite.getUses() > oldUses;
        }).findAny().orElse(null);
    }

    @Override
    public void onGenericGuildInvite(@NotNull GenericGuildInviteEvent event) {
        guild.retrieveInvites().queue(newInvites -> {
            this.invites = newInvites;
        });
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        @NotNull final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @NotNull final Member member = event.getMember();
        if(member.getUser().isBot()) return;

        guild.retrieveInvites().queue(newInvites -> {
            this.invites = newInvites;

            @Nullable final Invite lastInviteUsed = this.retrieveLastInviteUsed(invites, newInvites);

            @Nullable final User inviterUser = lastInviteUsed != null ? lastInviteUsed.getInviter() : null;
            if(inviterUser != null) {
                @Nullable final Member inviterMember = guild.getMember(inviterUser);
                if(inviterMember != null) {
                    @NotNull final MemberCache inviterCache = membersController.retrieveMemberCache(inviterMember);
                    @NotNull final MemberInvitesCache inviterInvitesCaches = inviterCache.retrieveInvitesCache();

                    inviterInvitesCaches.increaseTotal(member, lastInviteUsed.getCode(), 1);
                }
            }

            @Nullable final TextChannel targetChannel = settingsController.retrieveSetting(GuildSetting.GUILD_MEMBERS_CHANNEL);
            if(targetChannel != null) {
                @NotNull final StringJoiner newMemberMessage = new StringJoiner(" ");

                newMemberMessage.add(String.format("> Bienvenue à %s sur le serveur, nous sommes désormais **%s membres** !", member.getAsMention(), IntegerUtils.formatNumberWithEmojis(clientCache.getPresenceController().getTotalHumans())));
                if(inviterUser != null) newMemberMessage.add(String.format("Remercions @**%s** pour l'avoir invité !", inviterUser.getEffectiveName()));

                targetChannel.sendMessage(newMemberMessage.add(":confetti_ball:").toString()).queue();
            }
        });
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        final Member member = event.getMember();
        if(member == null || member.getUser().isBot()) return;

        guild.retrieveInvites().queue(newInvites -> {
            this.invites = newInvites;

            @Nullable final TextChannel targetChannel = settingsController.retrieveSetting(GuildSetting.GUILD_MEMBERS_CHANNEL);
            if(targetChannel != null) {
                final StringJoiner leftMemberMessage = new StringJoiner(" ");

                leftMemberMessage.add(String.format("> Au revoir @**%s**, en espérant te revoir prochainement ! Nous sommes désormais **%s membres** !", member.getUser().getEffectiveName(), IntegerUtils.formatNumberWithEmojis(clientCache.getPresenceController().getTotalHumans())));
                targetChannel.sendMessage(leftMemberMessage.add(":wave:").toString()).queue();
            }

            CompletableFuture.runAsync(() -> {
                databaseController.executeQuery("SELECT member_id FROM guilds_members_invites_history WHERE guild_id = ? AND invited_date=(SELECT MAX(invited_date) FROM guilds_members_invites_history WHERE invited_id = ?)", invites -> {
                    if(invites.next()) {
                        @NotNull final String memberID = invites.getString("member_id");
                        @Nullable final Member inviterMember = guild.getMemberById(memberID);

                        if(inviterMember != null) {
                            guildCache.getMembersController().retrieveMemberCache(inviterMember).retrieveInvitesCache().increaseLeft(1);
                        } else {
                            databaseController.executeUpdate("UPDATE guilds_members_invites SET lefts=lefts-1 WHERE guild_id = ? AND member_id = ?", guild.getId(), memberID);
                        }
                    }
                }, guild.getId(), member.getId());
            });
        });
    }

}
