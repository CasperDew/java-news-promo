package com.example.news.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Хранит загруженные картинки постов на диске и отдаёт их по адресу /uploads/имя-файла.
 */
@Getter
@Service
public class ImageStorageService {
    public static final long MAX_SIZE = 5L * 1024 * 1024;
    public static final String URL_PREFIX = "/uploads/";

    private final Path dir;

    public ImageStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.dir);
    }

    /**
     * Сохраняет файл и возвращает ссылку на него. Тип определяется по содержимому, а не по имени файла.
     */
    public String store(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("Файл больше 5 МБ");
        try {
            byte[] bytes = file.getBytes();
            String ext = detectExtension(bytes);
            if (ext == null) throw new IllegalArgumentException("Поддерживаются только JPG, PNG, GIF и WEBP");
            String name = UUID.randomUUID() + "." + ext;
            Files.write(dir.resolve(name), bytes, StandardOpenOption.CREATE_NEW);
            return URL_PREFIX + name;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить файл", e);
        }
    }

    public void deleteIfLocal(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) return;
        String name = url.substring(URL_PREFIX.length());
        if (!name.matches("[0-9a-f-]+\\.(jpg|png|gif|webp)")) return;
        try {
            Files.deleteIfExists(dir.resolve(name));
        } catch (IOException ignored) {
            // файл останется на диске, на работу сайта это не влияет
        }
    }

    private static String detectExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') return "gif";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }
}



