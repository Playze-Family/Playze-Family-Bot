package fr.jielos.playzefamilybot.utils.common.objects;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeUtils {

    @NotNull
    public static String formatDuration(int seconds, int maxParts) {
        @NotNull final Duration duration = Duration.ofSeconds(seconds);
        @NotNull final List<String> strings = new ArrayList<>();

        if(duration.toDaysPart() > 0) strings.add(duration.toDaysPart() + " jours");
        if(duration.toHoursPart() > 0 && strings.size() < maxParts) strings.add(duration.toHoursPart() + " heures");
        if(duration.toMinutesPart() > 0 && strings.size() < maxParts) strings.add(duration.toMinutesPart() + " minutes");
        if(duration.toSecondsPart() > 0 && strings.size() < maxParts) strings.add(duration.toSecondsPart() + " secondes");

        return String.join(", ", strings);
    }

    @NotNull
    public static Date getDateInMinutes(Date date, int minutes) {
        return new Date(date.getTime() + ((long) minutes* 60000));
    }

    @NotNull
    public static Date getDateInDays(Date date, int days) {
        return new Date(date.getTime() + ((long) (days*8.64e+7)));
    }

}
