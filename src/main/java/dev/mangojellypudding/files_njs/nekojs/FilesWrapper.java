package dev.mangojellypudding.files_njs.nekojs;

import dev.mangojellypudding.files_njs.FilesNJS;
import lombok.NoArgsConstructor;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@NoArgsConstructor
public class FilesWrapper {
    private Path validateAndNormalizePath(String path) {
        Path minecraftDir = FMLPaths.GAMEDIR.get().normalize().toAbsolutePath();
        path = path.replace('\\', '/');
        return minecraftDir.resolve(path).normalize().toAbsolutePath();
    }

    public String readFile(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            return Files.readString(normalizedPath);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error reading file: {}", path, e);
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    public List<String> readLines(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            return Files.readAllLines(normalizedPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error reading lines from file: {}", path, e);
            throw new RuntimeException("Failed to read lines from file: " + path, e);
        }
    }

    public void writeFile(String path, String content) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            Files.writeString(normalizedPath, content);

            boolean isNewFile = !Files.exists(normalizedPath);

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

            if (isNewFile) {
                FileEventJS event = new FileEventJS(path, content, "created", server);
                FileEvents.FILE_CREATED.post(event);
            } else {
                FileEventJS event = new FileEventJS(path, content, "changed", server);
                FileEvents.FILE_CHANGED.post(event);
            }
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error writing file: {}", path, e);
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }

    public void writeLines(String path, List<String> lines) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            Files.write(normalizedPath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error writing lines to file: {}", path, e);
            throw new RuntimeException("Failed to write lines to file: " + path, e);
        }
    }

    public void appendFile(String path, String content) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            Files.writeString(normalizedPath, content,
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error appending to file: {}", path, e);
            throw new RuntimeException("Failed to append to file: " + path, e);
        }
    }

    public boolean exists(String path) {
        Path normalizedPath = validateAndNormalizePath(path);
        return Files.exists(normalizedPath);
    }

    public void createDirectory(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            Files.createDirectories(normalizedPath);
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            FileEventJS event = new FileEventJS(path, null, "directory_created", server);
            FileEvents.DIRECTORY_CREATED.post(event);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error creating directory: {}", path, e);
            throw new RuntimeException("Failed to create directory: " + path, e);
        }
    }

    public void delete(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            boolean isDirectory = Files.isDirectory(normalizedPath);

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            FileEventJS event = new FileEventJS(path, null, isDirectory ? "directory_deleted" : "deleted", server);

            Files.delete(normalizedPath);

            if (isDirectory) {
                FileEvents.DIRECTORY_DELETED.post(event);
            } else {
                FileEvents.FILE_DELETED.post(event);
            }
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error deleting file: {}", path, e);
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    public List<String> listFiles(String path) {
        Stream<Path> stream = null;
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            stream = Files.list(normalizedPath);
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error listing files: {}", path, e);
            throw new RuntimeException("Failed to list files: " + path, e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public List<String> listDirectories(String path) {
        Stream<Path> stream = null;
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            stream = Files.list(normalizedPath);
            return stream
                    .filter(Files::isDirectory)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error listing directories: {}", path, e);
            throw new RuntimeException("Failed to list directories: " + path, e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public void copy(String source, String target) {
        try {
            Path sourcePath = validateAndNormalizePath(source);
            Path targetPath = validateAndNormalizePath(target);

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            FileEventJS event = new FileEventJS(target, null, "copied", server);
            FileEvents.FILE_COPIED.post(event);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error copying file: {} -> {}", source, target, e);
            throw new RuntimeException("Failed to copy file: " + source + " -> " + target, e);
        }
    }

    public void move(String source, String target) {
        try {
            Path sourcePath = validateAndNormalizePath(source);
            Path targetPath = validateAndNormalizePath(target);

            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            String content = Files.readString(targetPath);
            FileEventJS event = new FileEventJS(target, content, "moved", server);
            FileEvents.FILE_MOVED.post(event);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error moving file: {} -> {}", source, target, e);
            throw new RuntimeException("Failed to move file: " + source + " -> " + target, e);
        }
    }


    public void appendLine(String path, String line) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            List<String> lines = new ArrayList<>();
            lines.add(line);
            Files.write(normalizedPath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error appending line to file: {}", path, e);
            throw new RuntimeException("Failed to append line to file: " + path, e);
        }
    }


    public void ensureDirectoryExists(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            if (!Files.exists(normalizedPath)) {
                Files.createDirectories(normalizedPath);
            }
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error creating directory: {}", path, e);
            throw new RuntimeException("Failed to create directory: " + path, e);
        }
    }

    public void saveJson(String path, String jsonContent) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);

            Path parent = normalizedPath.getParent();
            if (parent != null) {
                String parentPath = FMLPaths.GAMEDIR.get().relativize(parent).toString().replace('\\', '/');
                ensureDirectoryExists(parentPath);
            }

            writeFile(path, jsonContent);
        } catch (RuntimeException e) {
            FilesNJS.LOGGER.error("Error saving JSON file: {}", path, e);
            throw new RuntimeException("Failed to save JSON file: " + path, e);
        }
    }

    public void saveScript(String path, String scriptContent) {
        try {
            if (!path.endsWith(".js")) {
                path += ".js";
            }

            Path normalizedPath = validateAndNormalizePath(path);

            Path parent = normalizedPath.getParent();
            if (parent != null) {
                String parentPath = FMLPaths.GAMEDIR.get().relativize(parent).toString().replace('\\', '/');
                ensureDirectoryExists(parentPath);
            }

            String formattedScript = String.format(
                    """
                            // Generated by FilesNJS
                            // Created at: %s
                            
                            %s""",
                    java.time.LocalDateTime.now(),
                    scriptContent
            );

            writeFile(path, formattedScript);
        } catch (RuntimeException e) {
            FilesNJS.LOGGER.error("Error saving script file: {}", path, e);
            throw new RuntimeException("Failed to save script file: " + path, e);
        }
    }

    public List<String> readLastLines(String path, int n) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            List<String> allLines = Files.readAllLines(normalizedPath, StandardCharsets.UTF_8);
            int start = Math.max(0, allLines.size() - n);
            return allLines.subList(start, allLines.size());
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error reading last lines: {}", path, e);
            throw new RuntimeException("Failed to read last lines: " + path, e);
        }
    }

    public List<String> searchInFile(String path, String searchTerm) {
        Stream<String> stream = null;
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            stream = Files.lines(normalizedPath);
            return stream
                    .filter(line -> line.contains(searchTerm))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error searching in file: {}", path, e);
            throw new RuntimeException("Failed to search in file: " + path, e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public Map<String, Object> getFileInfo(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            Map<String, Object> info = new HashMap<>();

            info.put("exists", Files.exists(normalizedPath));
            if (Files.exists(normalizedPath)) {
                info.put("size", Files.size(normalizedPath));
                info.put("lastModified", Files.getLastModifiedTime(normalizedPath).toMillis());
                info.put("isDirectory", Files.isDirectory(normalizedPath));
                info.put("isFile", Files.isRegularFile(normalizedPath));
                info.put("isReadable", Files.isReadable(normalizedPath));
                info.put("isWritable", Files.isWritable(normalizedPath));
            }

            return info;
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error getting file info: {}", path, e);
            throw new RuntimeException("Failed to get file info: " + path, e);
        }
    }

    public List<String> listFilesRecursively(String path) {
        Stream<Path> stream = null;
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            List<String> files = new ArrayList<>();
            stream = Files.walk(normalizedPath);
            stream.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .forEach(files::add);

            return files;
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error listing files recursively: {}", path, e);
            throw new RuntimeException("Failed to list files recursively: " + path, e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public void copyFiles(String sourceDir, String targetDir, String pattern) {
        Stream<Path> stream = null;
        try {
            Path sourcePath = validateAndNormalizePath(sourceDir);
            Path targetPath = validateAndNormalizePath(targetDir);

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            stream = Files.walk(sourcePath);
            stream
                    .filter(path -> Files.isRegularFile(path) && matcher.matches(path.getFileName()))
                    .forEach(source -> {
                        try {
                            Path target = targetPath.resolve(sourcePath.relativize(source));
                            Files.createDirectories(target.getParent());
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            FilesNJS.LOGGER.error("Error copying file: {}", source, e);
                        }
                    });
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error in batch copy operation", e);
            throw new RuntimeException("Failed in batch copy operation", e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public void backupFile(String path) {
        try {
            Path sourcePath = validateAndNormalizePath(path);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupName = sourcePath.getFileName().toString() + "." + timestamp + ".backup";

            Path backupDir = Paths.get("kubejs/backups");
            Path backupPath = backupDir.resolve(backupName);
            validateAndNormalizePath(backupPath.toString());

            Files.createDirectories(backupDir);
            filesCopy(sourcePath, backupPath);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error creating backup: {}", path, e);
            throw new RuntimeException("Failed to create backup: " + path, e);
        }
    }

    public boolean isFileEmpty(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            return Files.size(normalizedPath) == 0;
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error checking if file is empty: {}", path, e);
            throw new RuntimeException("Failed to check if file is empty: " + path, e);
        }
    }

    public void mergeFiles(List<String> sourcePaths, String targetPath) {
        try {
            List<Path> normalizedSourcePaths = new ArrayList<>();
            for (String path : sourcePaths) {
                normalizedSourcePaths.add(validateAndNormalizePath(path));
            }

            Path normalizedTargetPath = validateAndNormalizePath(targetPath);

            List<String> mergedContent = new ArrayList<>();
            for (Path path : normalizedSourcePaths) {
                mergedContent.addAll(Files.readAllLines(path, StandardCharsets.UTF_8));
                mergedContent.add("");
            }

            Files.write(normalizedTargetPath, mergedContent, StandardCharsets.UTF_8);

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            FileEventJS event = new FileEventJS(targetPath, String.join("\n", mergedContent), "merged", server);
            FileEvents.FILES_MERGED.post(event);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error merging files to: {}", targetPath, e);
            throw new RuntimeException("Failed to merge files: " + targetPath, e);
        }
    }

    public void replaceInFile(String path, String search, String replace) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            String content = Files.readString(normalizedPath);
            String newContent = content.replace(search, replace);
            Files.writeString(normalizedPath, newContent);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error replacing content in file: {}", path, e);
            throw new RuntimeException("Failed to replace content in file: " + path, e);
        }
    }

    public void processLargeFile(String path, Consumer<String> lineProcessor) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            try (BufferedReader reader = Files.newBufferedReader(normalizedPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineProcessor.accept(line);
                }
            }
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error processing large file: {}", path, e);
            throw new RuntimeException("Failed to process large file: " + path, e);
        }
    }

    public String getFileMD5(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(Files.readAllBytes(normalizedPath));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            FilesNJS.LOGGER.error("Error calculating MD5 for file: {}", path, e);
            throw new RuntimeException("Failed to calculate MD5: " + path, e);
        }
    }

    public boolean compareFiles(String path1, String path2) {
        try {
            Path normalizedPath1 = validateAndNormalizePath(path1);
            Path normalizedPath2 = validateAndNormalizePath(path2);

            return Arrays.equals(
                    Files.readAllBytes(normalizedPath1),
                    Files.readAllBytes(normalizedPath2)
            );
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error comparing files: {} vs {}", path1, path2, e);
            throw new RuntimeException("Failed to compare files", e);
        }
    }

    public boolean createFiles(String path, String content) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            Path parentDir = normalizedPath.getParent();

            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.writeString(normalizedPath, content);

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            FileEventJS event = new FileEventJS(path, content, "created", server);
            FileEvents.FILE_CREATED.post(event);

            return true;
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error creating file: {}", path, e);
            return false;
        }
    }

    public void createZip(String sourcePath, String zipPath) {
        try {
            Path source = validateZipPath(sourcePath);

            if (!zipPath.toLowerCase().endsWith(".zip")) {
                zipPath = zipPath + ".zip";
            }

            Path zip = validateAndNormalizePath(zipPath);

            if (!Files.exists(source)) {
                throw new IOException("Source path does not exist: " + sourcePath);
            }

            if (Files.isDirectory(zip)) {
                String sourceFileName = source.getFileName().toString();
                zip = zip.resolve(sourceFileName + ".zip");
            }

            Path zipParent = zip.getParent();
            if (zipParent != null) {
                Files.createDirectories(zipParent);
            }

            Stream<Path> stream = null;
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
                if (Files.isDirectory(source)) {
                    stream = Files.walk(source);
                    stream.forEach(path -> {
                        try {
                            String relativePath = source.relativize(path).toString().replace('\\', '/');
                            if (Files.isDirectory(path)) {
                                if (!relativePath.isEmpty()) {
                                    relativePath += "/";
                                    zos.putNextEntry(new ZipEntry(relativePath));
                                    zos.closeEntry();
                                }
                            } else {
                                zos.putNextEntry(new ZipEntry(relativePath));
                                Files.copy(path, zos);
                                zos.closeEntry();
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                } else {
                    String fileName = source.getFileName().toString();
                    zos.putNextEntry(new ZipEntry(fileName));
                    Files.copy(source, zos);
                    zos.closeEntry();
                }
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            FilesNJS.LOGGER.info("Successfully created zip file: {}", zip);
        } catch (IOException | UncheckedIOException e) {
            FilesNJS.LOGGER.error("Error creating zip file: {}", zipPath, e);
            throw new RuntimeException("Failed to create zip file: " + zipPath, e);
        }
    }

    private Path validateZipPath(String path) {
        return Paths.get(FMLPaths.GAMEDIR.get().toString(), path).normalize();
    }

    private final Map<String, WatchService> watchServices = new HashMap<>();

    public void watchDirectory(String path, Consumer<Path> changeCallback) {
        Path normalizedPath = validateAndNormalizePath(path);

        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            normalizedPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error creating watch service: {}", path, e);
            throw new RuntimeException("Failed to create watch service: " + path, e);
        }

        watchServices.put(path, watchService);

        Thread watchThread = getThread(changeCallback, watchService, normalizedPath);
        watchThread.start();
    }

    private static @NonNull Thread getThread(Consumer<Path> changeCallback, WatchService watchService, Path normalizedPath) {
        Thread watchThread = new Thread(() -> {
            try {
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> watchedEvent : key.pollEvents()) {
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) watchedEvent;
                        Path changed = normalizedPath.resolve(pathEvent.context());
                        changeCallback.accept(changed);
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        watchThread.setDaemon(true);
        return watchThread;
    }

    public void stopWatching(String path) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            WatchService watchService = watchServices.remove(normalizedPath.toString());
            if (watchService != null) {
                try {
                    watchService.close();
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    FileEventJS event = new FileEventJS(path, null, "watch_stopped", server);
                    FileEvents.FILE_WATCH_STOPPED.post(event);
                } catch (IOException e) {
                    FilesNJS.LOGGER.error("Error closing file watcher: {}", path, e);
                    throw new RuntimeException("Failed to close file watcher: " + path, e);
                }
            }
        } catch (RuntimeException e) {
            FilesNJS.LOGGER.error("Error stopping file watcher: {}", path, e);
            throw e;
        }
    }

    public void watchContentChanges(String path, double threshold) {
        try {
            Path normalizedPath = validateAndNormalizePath(path);
            Path parentDir = normalizedPath.getParent();
            Path fileName = normalizedPath.getFileName();

            String originalContent = Files.readString(normalizedPath);

            String relativeParentDir = FMLPaths.GAMEDIR.get()
                    .relativize(parentDir)
                    .toString()
                    .replace('\\', '/');

            watchDirectory(relativeParentDir, changedPath -> {
                try {
                    if (changedPath.getFileName().equals(fileName) && Files.exists(changedPath)) {
                        String newContent = Files.readString(changedPath);
                        double similarity = calculateSimilarity(originalContent, newContent);
                        if (1.0 - similarity > threshold) {
                            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                            FileEventJS event = new FileEventJS(
                                    path,
                                    newContent,
                                    "content_changed_significantly",
                                    server
                            );
                            FileEvents.FILE_CONTENT_CHANGED_SIGNIFICANTLY.post(event);
                        }
                    }
                } catch (IOException e) {
                    FilesNJS.LOGGER.error("Error checking content changes: {}", path, e);
                }
            });
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error setting up content watch: {}", path, e);
            throw new RuntimeException("Failed to set up content watch: " + path, e);
        }
    }

    private double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }

        int[][] dp = new int[text1.length() + 1][text2.length() + 1];

        for (int i = 1; i <= text1.length(); i++) {
            for (int j = 1; j <= text2.length(); j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        int lcsLength = dp[text1.length()][text2.length()];
        int maxLength = Math.max(text1.length(), text2.length());

        return maxLength > 0 ? (double) lcsLength / maxLength : 1.0;
    }

    private Object currentTickListener = null;

    public void scheduleBackup(String path, int ticks) {
        Path normalizedPath = validateAndNormalizePath(path);

        if (ticks == 0) {
            doBackup(normalizedPath.toString());
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            if (currentTickListener != null) {
                NeoForge.EVENT_BUS.unregister(currentTickListener);
            }

            Object listener = getListener(path, ticks, normalizedPath);

            currentTickListener = listener;
            NeoForge.EVENT_BUS.register(listener);
        }
    }

    private @NonNull Object getListener(String path, int ticks, Path normalizedPath) {
        final int[] tickCounter = {0};

        return new Object() {
            @SubscribeEvent
            public void onServerTick(ServerTickEvent.Post event) {
                tickCounter[0]++;
                if (tickCounter[0] >= ticks) {
                    try {
                        doBackup(normalizedPath.toString());
                    } catch (Exception e) {
                        FilesNJS.LOGGER.error("Error during scheduled backup: {}", path, e);
                    }
                    NeoForge.EVENT_BUS.unregister(this);
                    currentTickListener = null;
                }
            }
        };
    }

    private void doBackup(String path) {
        try {
            Path sourcePath = validateAndNormalizePath(path);
            Path backupDir = Paths.get("kubejs/backups");
            Files.createDirectories(backupDir);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = sourcePath.getFileName().toString();
            String backupName = fileName + "." + timestamp + ".backup";

            Path backupPath = backupDir.resolve(backupName);
            validateAndNormalizePath(backupPath.toString());

            filesCopy(sourcePath, backupPath);

            cleanupOldBackups(backupDir, 5);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error creating backup: {}", path, e);
            throw new RuntimeException("Failed to create backup: " + path, e);
        }
    }

    private void filesCopy(Path sourcePath, Path backupPath) throws IOException {
        Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        FileEventJS event = new FileEventJS(
                backupPath.toString(),
                null,
                "backup_created",
                server
        );
        FileEvents.FILE_BACKUP_CREATED.post(event);
    }

    private void cleanupOldBackups(Path backupDir, int keepCount) throws IOException {
        if (!Files.exists(backupDir)) return;

        Stream<Path> stream = Files.list(backupDir);
        List<Path> backups = stream
                .filter(path -> path.toString().endsWith(".backup"))
                .sorted((a, b) -> {
                    try {
                        return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .toList();

        if (backups.size() > keepCount) {
            for (Path backup : backups.subList(keepCount, backups.size())) {
                Files.delete(backup);
            }
        }

        stream.close();
    }

    private FileEventJS createFileEvent(String path, String content, String type) {
        Path normalizedPath = validateAndNormalizePath(path);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return new FileEventJS(normalizedPath.toString(), content, type, server);
    }

    public void renameFile(String oldPath, String newPath) {
        try {
            Path sourcePath = validateAndNormalizePath(oldPath);
            Path targetPath = validateAndNormalizePath(newPath);

            String content = Files.readString(sourcePath);

            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            FileEventJS event = new FileEventJS(newPath, content, "renamed", server);
            FileEvents.FILE_RENAMED.post(event);
        } catch (IOException e) {
            FilesNJS.LOGGER.error("Error renaming file: {} -> {}", oldPath, newPath, e);
            throw new RuntimeException("Failed to rename file: " + oldPath + " -> " + newPath, e);
        }
    }
} 