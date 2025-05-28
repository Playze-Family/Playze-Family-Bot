package com.agonkolgeci.gebo.core.dev.members.custom_role;

import com.agonkolgeci.gebo.api.cache.CacheConnector;
import com.agonkolgeci.gebo.core.dev.members.MemberCache;
import com.agonkolgeci.gebo.core.dev.members.MemberComponent;
import com.agonkolgeci.gebo.core.settings.GuildSettingLegacy;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class MemberCustomRoleCache extends MemberComponent implements CacheConnector {

    @Getter @Nullable private String roleID;

    public MemberCustomRoleCache(@NotNull MemberCache memberCache) {
        super(memberCache);

        databaseManager.executeQuery("SELECT * FROM guilds_members_custom_ranks WHERE guild_id = ? AND member_id = ?", ranks -> {
            if(ranks.next()) {
                this.roleID = ranks.getString("role_id");
            }
        }, guild.getId(), member.getId());
    }

    public boolean hasPermissions() {
        if(member.hasPermission(Permission.ADMINISTRATOR)) return true;

        @Nullable final Role guildCustomRole = guildCache.getSettingsManager().retrieveSetting(GuildSettingLegacy.GUILD_MEMBERS_CUSTOM_ROLE);
        if(guildCustomRole == null) return false;

        return member.getRoles().contains(guildCustomRole);
    }

    @Nullable
    public Role retrieveRole() {
        if(roleID == null) return null;

        return guild.getRoleById(roleID);
    }

    @NotNull
    public CompletableFuture<Role> updateRole(@NotNull String name, @NotNull String emoji, @NotNull String color) {
        @NotNull final CompletableFuture<Role> finalRole = new CompletableFuture<>();

        @Nullable final Role currentRole = retrieveRole();
        if(currentRole == null) {
            guild.createRole().setName("Rôle personnalisé de @" + member.getUser().getEffectiveName()).setHoisted(false).setMentionable(false).setPermissions(0L).queue(newRole -> {
                @Nullable final Role guildCustomRole = guildCache.getSettingsManager().retrieveSetting(GuildSettingLegacy.GUILD_MEMBERS_CUSTOM_ROLE);
                if(guildCustomRole != null) {
                    guild.modifyRolePositions().selectPosition(newRole).moveUp(guildCustomRole.getPosition()).queue();
                }

                this.roleID = newRole.getId();

                finalRole.complete(newRole);
            });
        } else {
            finalRole.complete(currentRole);
        }

        return finalRole.thenApply(customRole -> {
            customRole.getManager().setName(String.format("%s • %s", emoji, name)).setColor(Color.decode(color)).queue();
            guild.addRoleToMember(member, customRole).queue();

            this.saveCache();

            return customRole;
        });
    }

    public void deleteRole() {
        this.roleID = null;

        this.deleteCache();
    }

    @Override
    public void saveCache() {
        if(roleID == null) return;

        CompletableFuture.runAsync(() -> {
            databaseManager.executeUpdate(
                "INSERT INTO guilds_members_custom_ranks(guild_id, member_id, role_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE role_id = VALUES(role_id)",
                guild.getId(), member.getId(),
                roleID
            );
        });
    }

    @Override
    public void deleteCache() {
        if(roleID == null) return;

        CompletableFuture.runAsync(() -> {
            databaseManager.executeUpdate(
                "DELETE FROM guilds_members_custom_ranks WHERE guild_id = ? AND member_id = ? AND role_id = ?",
                guild.getId(), member.getId()
            );
        });
    }

}