package fr.jielos.playzefamilybot.utils.common.objects;

import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Nullable;

public class BooleanUtils {

    public static boolean parseBoolean(@Nullable String value) {
        try {
            Checks.notNull(value, "Value");

            return Boolean.parseBoolean(value);
        } catch (Exception exception) {
            return false;
        }
    }

}
