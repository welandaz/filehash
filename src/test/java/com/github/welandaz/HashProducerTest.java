package com.github.welandaz;

import com.github.welandaz.utils.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HashProducerTest {

    private static final Path TMP_ROOT = Paths.get("/tmp").resolve("test");

    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("indow");

    @BeforeMethod
    public void setUp() throws IOException {
        FileUtils.delete(TMP_ROOT);
        FileUtils.ensureDirectory(TMP_ROOT);
    }

    @Test
    public void testShouldThrowExceptionIfInputFileNotFound() {
        assertThatThrownBy(() -> HashProducer.path(Paths.get("unknown_path")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File [unknown_path] not found");
    }

    @Test
    public void testShouldProduceEmptyHashForEmptyDirectory() {
        final Map<Path, String> hashes = HashProducer.path(TMP_ROOT).toMap();

        assertThat(hashes).isEmpty();
    }

    @Test
    public void testShouldProduceEmptyHashForEmptyDirectoryHierarchy() {
        FileUtils.createPath(TMP_ROOT, "input/foo/bar/buzz");
        FileUtils.createPath(TMP_ROOT, "input/dir/subDir");

        final Map<Path, String> hashes = HashProducer.path(TMP_ROOT).toMap();

        assertThat(hashes).isEmpty();
    }

    @Test
    public void testShouldProduceCorrectHashForSingleFile() throws IOException {
        final Path file = FileUtils.createPath(TMP_ROOT, "file");

        FileUtils.write(file, "text to be written to file".getBytes());

        final Map<Path, String> hashes = HashProducer.path(file).toMap();

        assertThat(hashes).isNotEmpty();
        assertThat(hashes)
                .containsOnlyKeys(file)
                .containsValue("69bf414bf3c9e4a1f9b08bc1bdfbf758b2deb65225eed120e4ff80a065099bd3cfee2f8b815fa2e64206eb447fbd1690a3d196f344852df68f3d45dc820a2cad");
    }

    @Test
    public void testShouldProduceSameHashForDirectoryAndFileIfOnlyOneFile() throws IOException {
        final Path file = FileUtils.createPath(TMP_ROOT.resolve("directory"), "file");

        FileUtils.write(file, "some ordinary text to be written to file".getBytes());

        final Map<Path, String> hashes = HashProducer.path(file.getParent()).toMap();

        assertThat(hashes).isNotEmpty();
        assertThat(hashes)
                .containsEntry(file, "9cdf018f1db1fd4bda024bbe96031af573157ae414e9ba7dba9b7f214468b4588d60fb2a874e49f3f715cf1cce23a5368c266c9105566720b3436b8032000e40")
                .containsEntry(file.getParent(), "9cda1a096f18fd61b71de18e0cd6a77877b9b485eaec81860dc398e76ff19d8b78e9964b9e75e225567dbffba6a59732209c6e64a5fdfb00791a3941435a7a2d");
    }

    @Test
    public void testShouldProduceHashForDirectoryAndFilesWithinIt() throws IOException {
        final Path fileA = FileUtils.createPath(TMP_ROOT.resolve("directory"), "fileA");
        final Path fileB = FileUtils.createPath(TMP_ROOT.resolve("directory"), "fileB");
        final Path fileC = FileUtils.createPath(TMP_ROOT.resolve("directory"), "fileC");

        FileUtils.write(fileA, "some ordinary text to be written to fileA".getBytes());
        FileUtils.write(fileB, "another ordinary text to be written to fileB".getBytes());
        FileUtils.write(fileC, "yet another ordinary text to be written to fileC".getBytes());

        final Map<Path, String> hashes = HashProducer.path(fileA.getParent()).toMap();

        assertThat(hashes).isNotEmpty();
        assertThat(hashes)
                .containsEntry(fileA, "c4d1ad5b60bfdb6f0083604a6dc68f00cd7a8f59b8e773cde6d4cd7fce83a75abbd99d6433223d6b01ecfbd6a569522c8ed958f4663e834cb7b13a75e88c50ca")
                .containsEntry(fileB, "a91b806f1b33c5664f1b9557f3989f2689db94fefe0a09462806e8ac9c228d3f92d07d933d8639f7e4766e967a4f746cc4e9766a761f1bc82c81897b211daade")
                .containsEntry(fileC, "fbfba520b725c4ad01ebe1c673ca763a73469aae13fd5c71557c57f56769333ec805c76676f1818f70b8240244bb635064ef5e43c85324cd3277602c4ddbbb12")
                .containsEntry(fileA.getParent(), "8519de22fe13b44b0f5465a3c7dd5b6c9ae6285bf3b214c9412b7e9ab2f047ebbbfa49c48199ef3065ecadede175c89712afe1b8db46a2b6a853686fb4a0aacc");
    }

    @Test
    public void testShouldProductHashForDirectoryWithMultipleDirectoriesAndFiles() {
        final URL resource = getClass().getResource(getClass().getSimpleName() + "/" + "input");

        final String path = resource.getPath();
        final Path input = Paths.get(IS_WINDOWS && path.startsWith("/") ? path.substring(1) : path);

        final Map<Path, String> hashes = HashProducer.path(input).toMap();

        assertThat(hashes).isNotEmpty();
        assertThat(hashes)
                .containsEntry(input, "6dd415b8f89a52dd3ce277946150f1df6ea98a89296d0574db69b1fbc4d0aade51abba041529309abfbf07897808edb31a4a6b73a9b7c79fce20476062f6288a")
                .containsEntry(input.resolve("bar"), "6ef01eac687a58a3b28d924f3fa0641b7629356dfca436beb457424d649d4a64faf60b228c3738a3c75da49052264c92135f8aa296cdaad0d4800a7496f88e62")
                .containsEntry(input.resolve("bar/fileA.dat"), "af371785c4fecf30acdd648a7d4d649901eeb67536206a9f517768f0851c0a06616f724b2a194e7bc0a762636c55fc34e0fcaf32f1e852682b2b07a9d7b7a9f9")
                .containsEntry(input.resolve("bar/fileB.dat"), "46868d0a185e942d2fd15739b60096feab4ccdc99139cca4c9db82325606115c8803a6bffe37d6e54c791330add6e1fc861bfa79399f01cc88eed3fcedce13d4")
                .containsEntry(input.resolve("bar/fileC.dat"), "c1e42aa0c8908c9c3d49879a4fc04a59a755735418ddc3a200e911673da188bf46f67818972eac54b38422895391c82b2b0e0cf34aea9468c3ad73c2d0ffa912")
                .containsEntry(input.resolve("faz"), "9f0c752149eb2699f077215798e03f16837ec85fda55a57efeee6480e8ee43971092deec7ff553476d53f0760d637d41b2c31be2b4ef55614ab5d17ab0f8f6dc")
                .containsEntry(input.resolve("faz/fileD.dat"), "9dd88c920d86ac24112eb692e87b047bb6e69cd413593b009af62a29a71daa68f094dd3340976ae9b8e5d8e5d66d964179409c049103f91f3ccba80d9de63b7a")
                .containsEntry(input.resolve("faz/fileE.dat"), "40c9964826072dbebe00ea99db34a8c8268088738de8d2a9c02743d0eed36a018adf122bacd789cc569ba2f5f54c75191683e3f252486bf71a5824ae99e20017");
    }
}