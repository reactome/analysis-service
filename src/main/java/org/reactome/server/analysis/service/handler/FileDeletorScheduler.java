package org.reactome.server.analysis.service.handler;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Component
@EnableScheduling
public class FileDeletorScheduler {

    @Value("${analysis.export.temp.folder}")
    private String exportedFilesTempDir;

    // keep the file for 15 minutes on the server then delete
    private final static int CUTOFF = 15;

    @Scheduled(cron = "0 0/15 * * * * ") // every 15 minutes
    public void quarterHourDeleteFiles() throws IOException {
        Path path = Paths.get(exportedFilesTempDir);
        Files.walk(path)
                .filter(filePath -> Files.isRegularFile(filePath))
                .forEach(this::deleteOlderFiles);
    }

    private void deleteOlderFiles(Path path) {
        FileTime fileTime = null;
        try {
            fileTime = Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocalDateTime now = LocalDateTime.now().minusMinutes(FileDeletorScheduler.CUTOFF);
        LocalDateTime convertedFileTime = null;
        if (fileTime != null) {
            convertedFileTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
        }

        if (convertedFileTime != null && convertedFileTime.isBefore(now)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
