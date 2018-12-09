package com.github.welandaz.utils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;

/**
 * Utility methods for working with files/directories
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * This function ensures existence of parent directories of a file(directory)
     *
     * @param path - path, where to check existence of parents
     */
    public static void ensureParent(@Nonnull final Path path) {
        Objects.requireNonNull(path, "path must not be null");

        ensureDirectory(path.getParent());
    }

    /**
     * This function is identical to {@link Files#createDirectories(Path, FileAttribute[])} with the exception,
     * that it catches all the exceptions, and rethrows an instance of {@link UncheckedIOException)}
     * which doesn't force clients to explicitly declare try{}catch{} blocks
     *
     * @param path - the directory to create
     */
    public static void ensureDirectory(@Nonnull final Path path) {
        try {
            Files.createDirectories(Objects.requireNonNull(path, "path must not be null"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * This function behaves identically to the {@link Files#write(Path, byte[], OpenOption...)}, but it also
     * ensures that all parent directories are created.
     *
     * @param path  - the path to the file
     * @param bytes - the byte array with the bytes to write
     * @throws IOException - if an I/O error occurs writing to or creating the file
     */
    public static void write(@Nonnull final Path path, @Nonnull final byte[] bytes) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(bytes, "bytes array must not be null");

        ensureParent(path);

        Files.write(path, bytes, StandardOpenOption.CREATE);
    }

    /**
     * This function resolves the root with required paths and creates all parent directories for the resulted path
     *
     * @param root - root directory
     * @param name - chain of directories to resolve to
     * @return - resolved path, with all its parent directories created
     */
    @Nonnull
    public static Path createPath(@Nonnull final Path root, @Nonnull final String name) {
        Objects.requireNonNull(root, "root must not be null");
        Objects.requireNonNull(name, "name must not be null");

        final Path tmpPath = root.resolve(name.startsWith("/") || name.startsWith("\\") ? name.substring(1) : name);

        FileUtils.ensureParent(tmpPath);

        return tmpPath;
    }

    /**
     * Recursively deletes specified path
     *
     * @param path - path to delete
     * @throws IOException - if an I/O error is thrown
     */
    public static void delete(@Nonnull final Path path) throws IOException {
        if (Files.exists(Objects.requireNonNull(path, "path must not be null"))) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(path);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path path, final IOException e) throws IOException {
                    Files.delete(path);

                    return FileVisitResult.CONTINUE;
                }

            });
        }
    }

}
