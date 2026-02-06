/*
 * JIRA SERVICE MANAGEMENT Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for KristaMediaClient.
 * Tests file upload, download, and compression scenarios.
 */
@DisplayName("KristaMediaClient Integration Tests")
class KristaMediaClientIntegrationTest {

    private KristaMediaClient kristaMediaClient;
    private FileRepository mockFileRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        kristaMediaClient = new KristaMediaClient();
        mockFileRepository = mock(FileRepository.class);
        
        // Use reflection to inject the mock FileRepository
        try {
            java.lang.reflect.Field field = KristaMediaClient.class.getDeclaredField("fileRepository");
            field.setAccessible(true);
            field.set(kristaMediaClient, mockFileRepository);
        } catch (Exception e) {
            fail("Failed to inject mock FileRepository: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should upload supported file format")
    void testToKristaFileWithSupportedFormat() throws IOException {
        // Arrange
        File testFile = createTempFile("test.txt", "Test content");
        FileHandle mockFileHandle = mock(FileHandle.class);
        app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);
        
        when(mockFileRepository.createNewFileByName("test.txt")).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);
        doNothing().when(mockFileHandle).setContent(any(InputStream.class));
        doNothing().when(mockFileHandle).close();
        
        // Act
        app.krista.model.base.File result = kristaMediaClient.toKristaFile(testFile);
        
        // Assert
        assertNotNull(result);
        verify(mockFileRepository).createNewFileByName("test.txt");
        verify(mockFileHandle).setContent(any(FileInputStream.class));
        verify(mockFileHandle).getFile();
    }

    @Test
    @DisplayName("Should compress and upload unsupported HTML file")
    void testToKristaFileWithHtmlFormat() throws IOException {
        // Arrange
        File testFile = createTempFile("test.html", "<html><body>Test</body></html>");
        FileHandle mockFileHandle = mock(FileHandle.class);
        app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);
        
        when(mockFileRepository.createNewFileByName(contains(".zip"))).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);
        doNothing().when(mockFileHandle).setContent(any(InputStream.class));
        doNothing().when(mockFileHandle).close();
        
        // Act
        app.krista.model.base.File result = kristaMediaClient.toKristaFile(testFile);
        
        // Assert
        assertNotNull(result);
        verify(mockFileRepository).createNewFileByName(contains(".zip"));
    }

    @Test
    @DisplayName("Should compress and upload unsupported JS file")
    void testToKristaFileWithJsFormat() throws IOException {
        // Arrange
        File testFile = createTempFile("test.js", "console.log('test');");
        FileHandle mockFileHandle = mock(FileHandle.class);
        app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);
        
        when(mockFileRepository.createNewFileByName(contains(".zip"))).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);
        doNothing().when(mockFileHandle).setContent(any(InputStream.class));
        doNothing().when(mockFileHandle).close();
        
        // Act
        app.krista.model.base.File result = kristaMediaClient.toKristaFile(testFile);
        
        // Assert
        assertNotNull(result);
        verify(mockFileRepository).createNewFileByName(contains(".zip"));
    }

    @Test
    @DisplayName("Should compress and upload unsupported EXE file")
    void testToKristaFileWithExeFormat() throws IOException {
        // Arrange
        File testFile = createTempFile("test.exe", "binary content");
        FileHandle mockFileHandle = mock(FileHandle.class);
        app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);
        
        when(mockFileRepository.createNewFileByName(contains(".zip"))).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);
        doNothing().when(mockFileHandle).setContent(any(InputStream.class));
        doNothing().when(mockFileHandle).close();
        
        // Act
        app.krista.model.base.File result = kristaMediaClient.toKristaFile(testFile);
        
        // Assert
        assertNotNull(result);
        verify(mockFileRepository).createNewFileByName(contains(".zip"));
    }

    @Test
    @DisplayName("Should download file from Krista media server")
    void testToJavaFile() throws IOException {
        // Arrange
        app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);
        when(mockKristaFile.getFileName()).thenReturn("download.txt");

        FileHandle mockFileHandle = mock(FileHandle.class);
        ByteArrayInputStream inputStream = new ByteArrayInputStream("Downloaded content".getBytes());

        when(mockFileRepository.getFile(mockKristaFile)).thenReturn(mockFileHandle);
        when(mockFileHandle.getContent()).thenReturn(inputStream);
        doNothing().when(mockFileHandle).close();

        // Act
        File result = kristaMediaClient.toJavaFile(mockKristaFile);

        // Assert
        assertNotNull(result);
        assertEquals("download.txt", result.getName());
        verify(mockFileRepository).getFile(mockKristaFile);
        verify(mockFileHandle).getContent();

        // Cleanup
        if (result.exists()) {
            result.delete();
        }
    }

    @Test
    @DisplayName("Should handle file without extension")
    void testToKristaFileWithoutExtension() throws IOException {
        // Arrange
        File testFile = createTempFile("testfile", "Test content");
        FileHandle mockFileHandle = mock(FileHandle.class);
        app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);

        when(mockFileRepository.createNewFileByName("testfile")).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);
        doNothing().when(mockFileHandle).setContent(any(InputStream.class));
        doNothing().when(mockFileHandle).close();

        // Act
        app.krista.model.base.File result = kristaMediaClient.toKristaFile(testFile);

        // Assert
        assertNotNull(result);
        verify(mockFileRepository).createNewFileByName("testfile");
    }

    @Test
    @DisplayName("Should test compressFile static method")
    void testCompressFileStaticMethod() throws IOException {
        // Arrange
        File sourceFile = createTempFile("source.txt", "Content to compress");
        String zipFilePath = tempDir.resolve("output.zip").toString();

        // Act
        KristaMediaClient.compressFile(zipFilePath, sourceFile.getAbsolutePath());

        // Assert
        File zipFile = new File(zipFilePath);
        assertTrue(zipFile.exists());
        assertTrue(zipFile.length() > 0);

        // Verify zip content
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            assertEquals("source.txt", entry.getName());
        }
    }

    @Test
    @DisplayName("Should handle all unsupported file formats")
    void testAllUnsupportedFormats() throws IOException {
        String[] unsupportedFormats = {"html", "php5", "pht", "phtml", "shtml", "asa", "cer", "asax", "swf", "xap", "jsp", "exe", "js"};

        for (String format : unsupportedFormats) {
            File testFile = createTempFile("test." + format, "test content");
            FileHandle mockFileHandle = mock(FileHandle.class);
            app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);

            when(mockFileRepository.createNewFileByName(contains(".zip"))).thenReturn(mockFileHandle);
            when(mockFileHandle.getFile()).thenReturn(mockKristaFile);
            doNothing().when(mockFileHandle).setContent(any(InputStream.class));
            doNothing().when(mockFileHandle).close();

            app.krista.model.base.File result = kristaMediaClient.toKristaFile(testFile);
            assertNotNull(result, "Failed for format: " + format);
        }
    }

    @Test
    @DisplayName("Should handle supported file formats")
    void testSupportedFormats() throws IOException {
        String[] supportedFormats = {"txt", "pdf", "doc", "docx", "jpg", "png"};

        for (String format : supportedFormats) {
            File testFile = createTempFile("test." + format, "test content");
            FileHandle mockFileHandle = mock(FileHandle.class);
            app.krista.model.base.File mockKristaFile = mock(app.krista.model.base.File.class);

            when(mockFileRepository.createNewFileByName("test." + format)).thenReturn(mockFileHandle);
            when(mockFileHandle.getFile()).thenReturn(mockKristaFile);
            doNothing().when(mockFileHandle).setContent(any(InputStream.class));
            doNothing().when(mockFileHandle).close();

            app.krista.model.base.File result = kristaMediaClient.toKristaFile(testFile);
            assertNotNull(result, "Failed for format: " + format);
            verify(mockFileRepository).createNewFileByName("test." + format);
        }
    }

    private File createTempFile(String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
}