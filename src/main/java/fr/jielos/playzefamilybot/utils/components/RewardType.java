package fr.jielos.playzefamilybot.utils.components;

import fr.jielos.playzefamilybot.client.guilds.members.MemberCache;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public enum RewardType {

    TEXT("Texte", ObjectType.STRING),

    MEMBER_LEVEL("Niveau(x)", ObjectType.INTEGER) {
        @NotNull
        @Override
        public String format(@NotNull Object value) {
            return value + " niveau(x)";
        }

        @Override
        public void giveReward(@NotNull MemberCache memberCache, @NotNull Object value) {
            memberCache.retrieveProfileCache().addXP((int) value, 0);
        }
    },

    MEMBER_XP("Point(s) d'expérience(s)", ObjectType.INTEGER) {
        @NotNull
        @Override
        public String format(@NotNull Object value) {
            return value + " point(s) d'expérience(s)";
        }

        @Override
        public void giveReward(@NotNull MemberCache memberCache, @NotNull Object value) {
            memberCache.retrieveProfileCache().addXP(0, (int) value);
        }
    },

    GUILD_ROLE("Rôle", ObjectType.GUILD_ROLE) {
        @Override
        public void giveReward(@NotNull MemberCache memberCache, @NotNull Object value) {
            memberCache.getGuild().addRoleToMember(memberCache.getMember(), (Role) value).queue();
        }
    };

    @Getter @NotNull private final String label;
    @Getter @NotNull private final ObjectType required;

    RewardType(@NotNull String label, @NotNull ObjectType required) {
        this.label = label;
        this.required = required;
    }

    @NotNull
    public String format(@NotNull Object value) {
        return required.format(value);
    }

    public void giveReward(@NotNull MemberCache memberCache, @NotNull Object value) {}

    public static void giveRewards(@NotNull MemberCache memberCache, @NotNull Map<RewardType, String> rewards) {
        for(@NotNull Map.Entry<RewardType, String> reward : rewards.entrySet()) {
            @NotNull final RewardType rewardType = reward.getKey();
            @NotNull final String value = reward.getValue();

            @Nullable final Object object = rewardType.getRequired().parse(memberCache.getGuild(), value);
            if(object == null) continue;

            rewardType.giveReward(memberCache, object);
        }
    }

}
