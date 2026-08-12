package br.com.fiap.fiapx.processor.infra.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class FfmpegVideoProcessor {

    public File extractFramesToZip(File videoFile, String videoId) throws Exception {
        Path framesDir = Files.createTempDirectory("frames_" + videoId);
        String outputPattern = framesDir.resolve("frame_%04d.jpg").toString();

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", videoFile.getAbsolutePath(),
                "-vf", "fps=1",
                "-q:v", "2",
                outputPattern
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(line -> log.debug("ffmpeg: {}", line));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg falhou com código: " + exitCode);
        }

        File zipFile = Files.createTempFile("zip_" + videoId, ".zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.list(framesDir).sorted().forEach(frame -> {
                try {
                    zos.putNextEntry(new ZipEntry(frame.getFileName().toString()));
                    Files.copy(frame, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

        deleteDirectory(framesDir.toFile());
        log.info("Frames extracted and zipped for video {}: {} bytes", videoId, zipFile.length());
        return zipFile;
    }

    private void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectory(child);
            }
        }
        dir.delete();
    }
}
