package com.agonkolgeci.gebo.core.dev.members.profile.voice;

import com.agonkolgeci.gebo.utils.common.images.ImageUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public enum MemberVoiceState {

    ALIVE(ImageUtils.ICON_ONLINE),
    IDLE(ImageUtils.ICON_IDLE),
    DISCONNECTED(ImageUtils.ICON_OFFLINE),
    SELF_DEAFENED(ImageUtils.ICON_SELF_DEAFENED);

    @Getter @Nullable private final BufferedImage icon;

    MemberVoiceState(@Nullable BufferedImage icon) {
        this.icon = icon;
    }

}