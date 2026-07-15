package com.zk.projectboot.service;

import com.zk.projectboot.exception.FileStorageException;
import com.zk.projectboot.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceImplTest {

    @TempDir
    Path temporaryDirectory;

    private FileStorageServiceImpl service;
    private Path avatarDirectory;

    @BeforeEach
    void setUp() {
        service = new FileStorageServiceImpl();
        avatarDirectory = temporaryDirectory.resolve("avatars");
        ReflectionTestUtils.setField(service, "uploadDir", temporaryDirectory.toString());
        ReflectionTestUtils.setField(service, "avatarDir", avatarDirectory.toString());
        ReflectionTestUtils.setField(service, "coverDir", temporaryDirectory.resolve("covers").toString());
        service.init();
    }

    @Test
    void storesJpegPngAndWebpWhenSignatureMimeAndExtensionAgree() {
        assertStored(file("photo.jpg", "image/jpeg", bytes(0xFF, 0xD8, 0xFF, 0x00)), ".jpg");
        assertStored(file("photo.png", "image/png", bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)), ".png");
        assertStored(file("photo.webp", "image/webp", new byte[]{
                'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'}), ".webp");
    }

    @Test
    void rejectsGifAndContentThatDoesNotMatchItsDeclaration() {
        assertThrows(FileStorageException.class, () -> service.storeAvatar(
                file("photo.gif", "image/gif", new byte[]{'G', 'I', 'F', '8', '9', 'a'})));
        assertThrows(FileStorageException.class, () -> service.storeAvatar(
                file("photo.png", "image/png", "not an image".getBytes())));
        assertThrows(FileStorageException.class, () -> service.storeAvatar(
                file("photo.jpg", "image/jpeg", bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))));
    }

    @Test
    void discardsPathComponentsFromTheOriginalName() {
        String storedName = service.storeAvatar(
                file("../photo.png", "image/png", bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)));

        assertTrue(storedName.matches("[0-9a-f-]{36}\\.png"));
        assertFalse(storedName.contains(".."));
        assertTrue(Files.exists(avatarDirectory.resolve(storedName)));
    }

    private void assertStored(MockMultipartFile file, String extension) {
        String storedName = service.storeAvatar(file);
        assertTrue(storedName.endsWith(extension));
        assertTrue(Files.exists(avatarDirectory.resolve(storedName)));
    }

    private MockMultipartFile file(String name, String contentType, byte[] content) {
        return new MockMultipartFile("file", name, contentType, content);
    }

    private byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }
}
