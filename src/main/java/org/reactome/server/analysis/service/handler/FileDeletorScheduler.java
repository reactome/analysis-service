package org.reactome.server.analysis.service.handler;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@EnableScheduling
public class FileDeletorScheduler {

    @Value("${analysis.export.temp.folder}")
    private String exportedFilesTempDir;

    // keep the file for 15 minutes on the server then delete
    private final static int CUTOFF = 15;

    @Scheduled(cron = "0 0/15 * * * ? ") // every 15 minutes
    public void quarterHourDeleteFiles() throws IOException {
        Path dir = Paths.get(exportedFilesTempDir);
        if (isEmpty(dir)) return;
        List<Path> paths = listFiles(dir);
        paths.forEach(this::deleteOlderFiles);
    }

    // list all files from this path
    public static List<Path> listFiles(Path path) throws IOException {
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return result;
    }

    public boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                return !directory.iterator().hasNext();
            }
        }
        return false;
    }

    private void deleteOlderFiles(Path path) {
        if (!Files.exists(path)) return;
        FileTime fileTime = null;
        try {
            fileTime = Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Instant now = Instant.now().minus(FileDeletorScheduler.CUTOFF, ChronoUnit.MINUTES);

        if (fileTime != null && fileTime.toInstant().isBefore(now)) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
