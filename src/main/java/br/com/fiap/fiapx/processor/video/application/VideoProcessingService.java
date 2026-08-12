package br.com.fiap.fiapx.processor.video.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class VideoProcessingService {

    public byte[] process(InputStream videoStream, String originalFilename) throws Exception {
        Path tempDir = Files.createTempDirectory("fiapx-processing-");
        try {
            Path videoFile = tempDir.resolve("input_" + originalFilename);
            Files.copy(videoStream, videoFile, StandardCopyOption.REPLACE_EXISTING);

            Path framesDir = tempDir.resolve("frames");
            Files.createDirectories(framesDir);

            extractFrames(videoFile, framesDir);

            List<Path> frames = Files.list(framesDir)
                    .filter(p -> p.toString().endsWith(".jpg"))
                    .sorted()
                    .toList();

            if (frames.isEmpty()) {
                throw new RuntimeException("No frames extracted from video");
            }

            return zipFrames(frames);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private void extractFrames(Path videoFile, Path framesDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", videoFile.toAbsolutePath().toString(),
                "-vf", "fps=1",
                framesDir.resolve("frame_%04d.jpg").toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(line -> log.debug("ffmpeg: {}", line));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg exited with code: " + exitCode);
        }
    }

    private byte[] zipFrames(List<Path> frames) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Path frame : frames) {
                zos.putNextEntry(new ZipEntry(frame.getFileName().toString()));
                Files.copy(frame, zos);
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private void deleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            log.warn("Could not delete temp dir: {}", dir);
        }
    }
}
