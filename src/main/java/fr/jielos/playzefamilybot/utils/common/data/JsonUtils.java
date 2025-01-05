package fr.jielos.playzefamilybot.utils.common.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.members.MemberCache;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonUtils {

    public static final Gson GSON = new Gson();

    @NotNull
    public static List<Member> getMembersFromJSON(@NotNull Guild guild, @NotNull String json) {
        return new ArrayList<String>(GSON.fromJson(json, new TypeToken<List<String>>(){}.getType())).stream().map(guild::getMemberById).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @NotNull
    public static String getMembersAsJSON(@NotNull List<Member> members) {
        return GSON.toJson(members.stream().map(Member::getId).toList());
    }

    @NotNull
    public static String getMembersCachesAsJSON(@NotNull List<MemberCache> members) {
        return getMembersAsJSON(members.stream().map(MemberCache::getMember).collect(Collectors.toList()));
    }

    @NotNull
    public static List<MemberCache> getMembersCachesFromJSON(@NotNull GuildCache guildCache, @NotNull String json) {
        return getMembersFromJSON(guildCache.getGuild(), json).stream().map(member -> {
            try {
                return guildCache.getMembersController().retrieveMemberCache(member);
            } catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
    }

}
