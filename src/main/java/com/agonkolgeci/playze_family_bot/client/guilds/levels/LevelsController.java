package com.agonkolgeci.playze_family_bot.client.guilds.levels;

import com.agonkolgeci.playze_family_bot.api.cache.CacheConnector;
import com.agonkolgeci.playze_family_bot.api.events.member.GenericGuildMemberProfileUpdate;
import com.agonkolgeci.playze_family_bot.api.managers.Controller;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildCache;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildComponent;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.adapters.CommandLeaderboard;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.adapters.CommandLevel;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.adapters.CommandRewards;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.adapters.CommandXP;
import com.agonkolgeci.playze_family_bot.client.guilds.members.MembersController;
import com.agonkolgeci.playze_family_bot.client.guilds.members.profile.MemberProfileCache;
import com.agonkolgeci.playze_family_bot.client.guilds.members.profile.level.MemberProfileLevel;
import com.agonkolgeci.playze_family_bot.client.guilds.settings.GuildSetting;
import com.agonkolgeci.playze_family_bot.client.guilds.settings.SettingsController;
import com.agonkolgeci.playze_family_bot.utils.common.objects.IntegerUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ui.EmojiUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ui.MessageUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ui.RoleUtils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LevelsController extends GuildComponent implements Controller<LevelsController>, CacheConnector {

    @NotNull private final SettingsController settingsController;
    @NotNull private final MembersController membersController;

    @NotNull private final Map<Integer, List<Role>> rewards;

    public LevelsController(@NotNull GuildCache guildCache, @NotNull SettingsController settingsController, @NotNull MembersController membersController) {
        super(guildCache);
        this.settingsController = settingsController;

        this.membersController = membersController;

        this.rewards = new TreeMap<>();
    }

    @NotNull
    @Override
    public LevelsController load() {
        this.loadRewards();
        this.loadActiveMembers();

        instance.getEventsController().registerEventAdapter(this);

        instance.getCommandsController().registerCommandAdapter(new CommandLeaderboard(guildCache, this));
        instance.getCommandsController().registerCommandAdapter(new CommandLevel(guildCache, this));
        instance.getCommandsController().registerCommandAdapter(new CommandRewards(guildCache, this));
        instance.getCommandsController().registerCommandAdapter(new CommandXP(guildCache, this));

        return this;
    }

    private void loadRewards() {
         databaseController.executeQuery("SELECT * FROM guilds_levels_rewards WHERE guild_id = ?", rewards -> {
            while(rewards.next()) {
                final int level = rewards.getInt("level");

                @Nullable final Role role = guild.getRoleById(rewards.getString("role_id"));
                if(role == null) {
                    rewards.deleteRow();

                    continue;
                }

                this.initReward(level, role);
            }
        }, guild.getId());
    }

    private void loadActiveMembers() {
        for(@NotNull final VoiceChannel voiceChannel : guild.getVoiceChannels()) {
            if(!voiceChannel.getMembers().isEmpty()) {
                for(@NotNull final MemberProfileCache memberProfileCache : voiceChannel.getMembers().stream().filter(member -> !member.getUser().isBot()).map(member -> membersController.retrieveMemberCache(member).retrieveProfileCache()).filter(Objects::nonNull).toList()) {
                    memberProfileCache.checkVoiceState();
                }
            }
        }
    }

    public void initReward(int level, @NotNull Role role) {
        @NotNull final List<Role> roles = rewards.getOrDefault(level, new ArrayList<>());
        roles.add(role);

        this.rewards.put(level, roles);
    }

    public void addReward(int level, @NotNull Role role) {
        this.initReward(level, role);
        this.saveCache();
    }

    public void removeReward(int level, @NotNull Role role) {
        @Nullable final List<Role> roles = rewards.get(level);
        if(roles == null || !roles.contains(role)) return;

        roles.remove(role);

        this.rewards.put(level, roles);

        this.saveCache();
    }

    public void removeReward(int level) {
        this.rewards.remove(level);

        this.saveCache();
    }

    public void removeReward(@NotNull Role role) {
        @NotNull List<Integer> levels = rewards.entrySet().stream().filter(entry -> entry.getValue().contains(role)).map(Map.Entry::getKey).toList();
        for(int level : levels) {
            this.removeReward(level, role);
        }
    }

    @NotNull
    public Map<Integer, List<Role>> getRewards() {
        return rewards.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().filter(Objects::nonNull).toList(), (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Nullable
    public Role getReward(int level, @NotNull Role role) {
        return getRewards(level).stream().filter(r -> r == role).findFirst().orElse(null);
    }

    @NotNull
    public List<Role> getRewards(int level) {
        return getRewards().getOrDefault(level, new ArrayList<>());
    }

    @NotNull
    public Map<Integer, List<Role>> getRewards(@NotNull Role role) {
        return getRewards().entrySet().stream().filter(entry -> entry.getValue() == role).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @NotNull
    public List<Role> getRewards(@NotNull Member member, double maxLevel) {
        return getRewards().entrySet().stream().filter(entry -> entry.getKey() <= maxLevel).flatMap(entry -> entry.getValue().stream()).toList();
    }

    @NotNull
    public List<MemberProfileCache> retrieveActiveProfiles() {
        @NotNull final List<Member> members = new ArrayList<>();

        instance.getDatabaseController().executeQuery("SELECT * FROM guilds_members_profiles WHERE guild_id = ?", profiles -> {
             while(profiles.next()) {
                @Nullable final Member member = guild.getMemberById(profiles.getString("member_id"));
                if(member == null) continue;

                members.add(member);
            }
        }, guild.getId());

        return members.stream().map(member -> membersController.retrieveMemberCache(member).retrieveProfileCache()).sorted(Comparator.comparingInt(MemberProfileCache::getLevel).thenComparingInt(MemberProfileCache::getXp).reversed()).collect(Collectors.toList());
    }

    @Override
    public void saveCache() {
        for(@NotNull final Map.Entry<Integer, List<Role>> entry : getRewards().entrySet()) {
            final int level = entry.getKey();

            for(@NotNull final Role role : entry.getValue()) {
                CompletableFuture.runAsync(() -> {
                    databaseController.executeUpdate(
                        "INSERT IGNORE INTO guilds_levels_rewards(guild_id, level, role_id) VALUES(?, ?, ?)",
                        guild.getId(),
                        level, role.getId()
                    );
                });
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;

        @NotNull final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @Nullable final Member member = event.getMember();
        if(member == null) return;

        @NotNull final Message message = event.getMessage();

        if(isValidMessage(message)) {
            membersController.retrieveMemberCache(member).retrieveProfileCache().attributeXP(MemberProfileLevel.MESSAGE);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(!event.isFromGuild()) return;

        @NotNull final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @Nullable final Member member = event.getMember();
        if(member == null || member.getUser().isBot()) return;

        checkReactionValidity(event.getReaction()).thenAccept(isValid -> {
            if(isValid) {
                membersController.retrieveMemberCache(member).retrieveProfileCache().attributeXP(MemberProfileLevel.REACTION);
            }
        });
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if(!event.isFromGuild()) return;

        @NotNull final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @Nullable final Member member = event.getMember();
        if(member == null) return;
        if(member.getUser().isBot()) return;

        checkReactionValidity(event.getReaction()).thenAccept(isValid -> {
            if(isValid) {
                 membersController.retrieveMemberCache(member).retrieveProfileCache().takeXP(MemberProfileLevel.REACTION);
            }
        });
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        @NotNull final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @Nullable final Member member = event.getMember();
        if(member.getUser().isBot()) return;

        membersController.retrieveMemberCache(member).retrieveProfileCache().checkVoiceState();
    }

    private boolean isValidMessage(@NotNull Message message) {
        if(message.getAuthor().isBot()) return false;

        return true;
    }

    @SuppressWarnings("all")
    private CompletableFuture<Boolean> checkReactionValidity(@NotNull MessageReaction messageReaction) {
        @NotNull final MessageChannelUnion channel = messageReaction.getChannel();

        return channel.retrieveMessageById(messageReaction.getMessageId()).submit()
                .thenApply(message -> {
                    if(message.getMentions().mentionsEveryone()) return true;
                    if(message.getAuthor().isBot()) return true;

                    return false;
                });
    }

    @Override
    public void onGuildMemberProfileUpdate(@NotNull GenericGuildMemberProfileUpdate event) {
        @Nullable final Guild guild = event.getGuild();
        if(this.guild != guild) return;

        @NotNull final Member member = event.getMember();

        final int oldLevel = event.getOldLevel();
        final int oldXP = event.getOldXP();
        final int newLevel = event.getNewLevel();
        final int newXP = event.getNewXP();

        if(oldLevel == newLevel) return;

        final int intervalAnnounceXP = settingsController.retrieveSetting(GuildSetting.GUILD_INTERVAL_XP_LEVEL_UP);
        if(newLevel % intervalAnnounceXP != 0) return;

        @NotNull final List<Role> newRoles = RoleUtils.addRolesToMember(member, this.getRewards(member, newLevel));

        @Nullable final TextChannel targetChannel = settingsController.retrieveSetting(GuildSetting.GUILD_LEVELS_ANNOUNCEMENTS);
        if(targetChannel != null) {
            targetChannel.sendMessage(MessageUtils.format(
                    EmojiUtils.EXTERN_EMOJI_TADA_ANIMATED,
                    String.format(("Félicitations %s, vous venez d'atteindre le niveau %s !"), member.getAsMention(), IntegerUtils.formatNumberWithEmojis(newLevel)),
                    String.format((!newRoles.isEmpty() ? "- Vous remportez les rôle(s) suivant(s): %s." : ""), newRoles.stream().map(Role::getAsMention).collect(Collectors.joining(", "))))
            ).setAllowedMentions(List.of(Message.MentionType.USER)).queue(message -> {
                message.addReaction(EmojiUtils.EXTERN_EMOJI_TADA_ANIMATED).queue();
            });
        }
    }
}
