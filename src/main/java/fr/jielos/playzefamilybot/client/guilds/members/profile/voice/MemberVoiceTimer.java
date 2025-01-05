package fr.jielos.playzefamilybot.client.guilds.members.profile.voice;

import fr.jielos.playzefamilybot.client.guilds.members.profile.MemberProfileCache;
import fr.jielos.playzefamilybot.client.guilds.members.profile.level.MemberProfileLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class MemberVoiceTimer extends TimerTask {

    @NotNull private final MemberProfileCache memberProfileCache;
    @Getter private int seconds;

    public MemberVoiceTimer(@NotNull MemberProfileCache memberProfileCache) {
        this.memberProfileCache = memberProfileCache;

        new Timer().scheduleAtFixedRate(this, 0, 1000);
    }

    @Override
    public void run() {
        if(!memberProfileCache.isAlive()) return;
        if(!memberProfileCache.isConnected()) {
            this.delete();

            return;
        }

        if(seconds >= 60) {
            memberProfileCache.attributeXP(MemberProfileLevel.VOICE);
            memberProfileCache.increaseTimeSpentInVoice(seconds);

            seconds = 0;
        }

        seconds++;
    }

    public void delete() {
        this.cancel();

        memberProfileCache.increaseTimeSpentInVoice(seconds);
    }

}
