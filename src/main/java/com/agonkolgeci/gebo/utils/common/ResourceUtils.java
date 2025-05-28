package com.agonkolgeci.gebo.utils.common;

import com.agonkolgeci.gebo.utils.common.images.ImageUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ResourceUtils {

    public static final double MAX_SIZE_SUPPORTED = 10 * 1e+6; // 10 Mo

    @Nullable
    public static InputStream retrieveLocalInputStream(@NotNull String root, @NotNull String... path) {
        return ClassLoader.getSystemResourceAsStream(Path.of(root, path).toString());
    }

    @NotNull
    public static FileUpload getImageAsFileUpload(@NotNull BufferedImage image, @NotNull String name, @NotNull String formatName) {
        return FileUpload.fromData(ImageUtils.toInputStream(image, formatName), name+"."+formatName);
    }

    @NotNull
    public static Collection<FileUpload> retrieveAttachmentsFiles(@NotNull Collection<Message.Attachment> attachments) {
        return attachments.stream().map(attachment -> {
            try {
                return FileUpload.fromData(attachment.getProxy().download().get(), attachment.getFileName());
            } catch (InterruptedException | ExecutionException ignored) {}

            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
