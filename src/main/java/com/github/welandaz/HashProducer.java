package com.github.welandaz;

import com.github.welandaz.utils.StringUtils;
import com.github.welandaz.utils.ThrowingUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import static java.lang.String.format;

/**
 * A service class, used for traversing the file system tree using {@link Files#walkFileTree} via {@link FileVisitor}.
 * <p>
 * It accepts an instance of {@link Path} via static factory, validates its existence and allows the result
 * of hash calculation to be written to a file or an in-memory map
 */
public final class HashProducer {

    private final Path input;

    private MessageDigest messageDigest;
    private int byteArraySize;

    private HashProducer(final Path input, final Hash hash, final int byteArraySize) {
        this.input = input;
        this.messageDigest = hash.messageDigest();
        this.byteArraySize = byteArraySize;
    }

    /**
     * Factory method to create an instance of {@link HashProducer} class based on the input parameter.
     * Sets SHA512 as a default hash function and 8KB read byte array size
     *
     * @param input - path, from where to read files
     * @return - instance of {@link HashProducer}
     */
    @Nonnull
    public static HashProducer path(@Nonnull final Path input) {
        if (Files.exists(Objects.requireNonNull(input, "input path must not be null"))) {
            return new HashProducer(input, Hash.SHA512, 8192);
        } else {
            throw new IllegalArgumentException(format("File [%s] not found", input));
        }
    }

    /**
     * Sets a hash function to be used for hashing from available predefined options
     *
     * @param hash - hash function for hashing files
     * @return - instance of {@link HashProducer}
     */
    @Nonnull
    public HashProducer hash(@Nonnull final Hash hash) {
        this.messageDigest = Objects.requireNonNull(hash, "hash must not be null").messageDigest();

        return this;
    }

    /**
     * Allows users to pass their own custom instance of MessageDigest to be used for hashing
     *
     * @param messageDigest - MessageDigest object to be used for hashing
     * @return - instance of {@link HashProducer}
     */
    @Nonnull
    public HashProducer hash(@Nonnull final MessageDigest messageDigest) {
        this.messageDigest = Objects.requireNonNull(messageDigest, "messageDigest must not be null");

        return this;
    }

    /**
     * Allows users to pass their own amount of bytes to be read in one iteration.
     * The default value is 8K, however its possible to adjust it based on the needs
     *
     * @param byteArraySize - number of bytes to be read from file in one go
     * @return - instance of {@link HashProducer}
     */
    @Nonnull
    public HashProducer byteArraySize(final int byteArraySize) {
        this.byteArraySize = byteArraySize;

        return this;
    }

    /**
     * This method uses {@link HashProducer#consumeHashes(BiConsumer)} to traverse the file system,
     * and write file/directory hashes to specified output file
     *
     * @param output - output file, where result of hashing is stored
     */
    public void toFile(@Nonnull final Path output) {
        Objects.requireNonNull(output, "output file must not be null");

        try (final Writer writer = Files.newBufferedWriter(output)) {
            consumeHashes(ThrowingUtils.consume((path, hash) -> writer.write(path + ": " + hash + "\n")));
        } catch (final IOException e) {
            throw new UncheckedIOException(format("Error occurred while writing hashes to file [%s]", output.toString()), e);
        }
    }

    /**
     * This method uses {@link HashProducer#consumeHashes(BiConsumer)} to traverse the file system,
     * and writes file/directory hashes to a new HashMap instance
     */
    public Map<Path, String> toMap() {
        final Map<Path, String> hashes = new HashMap<>();
        try {
            consumeHashes(hashes::put);

            return hashes;
        } catch (final IOException e) {
            throw new UncheckedIOException(format("Error occurred while producing hashes from [%s] to memory map", input), e);
        }
    }

    /**
     * Traverses the file system, calculates hashes of files/directories and passes them to the specified consumer
     *
     * @param biConsumer - function, that aggregates data, based on its needs
     * @throws IOException - if an I/O error is thrown by a visitor
     */
    private void consumeHashes(final BiConsumer<Path, String> biConsumer) throws IOException {
        Files.walkFileTree(input, new FileVisitor<Path>() {
            private final Map<Path, Map<Path, String>> hashes = new HashMap<>();

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                hashes.put(dir, new TreeMap<>());

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException e) throws IOException {
                throw new IOException(format("Error occurred while processing [%s]", file.toString()), e);
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                final byte[] byteArray = new byte[byteArraySize];
                int bytesCount;
                try (final FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
                    while ((bytesCount = fileInputStream.read(byteArray)) != -1) {
                        messageDigest.update(byteArray, 0, bytesCount);
                    }
                }

                final String hash = StringUtils.toHexString(messageDigest.digest());

                updateParentHash(file, hash);

                biConsumer.accept(file, hash);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException e) {
                final Map<Path, String> directoryHashes = hashes.get(dir);
                if (!directoryHashes.isEmpty()) {
                    final String concatenatedHashes = String.join("", directoryHashes.values());

                    final String hash = StringUtils.toHexString(messageDigest.digest(concatenatedHashes.getBytes()));

                    hashes.remove(dir);

                    biConsumer.accept(dir, hash);

                    updateParentHash(dir, hash);
                }

                return FileVisitResult.CONTINUE;
            }

            private void updateParentHash(final Path file, final String hash) {
                final Path parent = file.getParent();

                hashes.computeIfPresent(parent, (path, parentHashes) -> {
                    parentHashes.put(file, hash);

                    return parentHashes;
                });
            }
        });
    }

}
