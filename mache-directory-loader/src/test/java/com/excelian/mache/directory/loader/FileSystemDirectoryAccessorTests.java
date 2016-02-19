package com.excelian.mache.directory.loader;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FileSystemDirectoryAccessorTests {

    @Test
    public void fileSystemShouldReturnAllFilesInDirectory() throws Exception {
        URL resource =  this.getClass().getClassLoader().getResource("Maps/");
        FileSystemDirectoryAccessor directoryAccessor = new FileSystemDirectoryAccessor(new File(resource.getPath()));

        List<String> testDirectory = directoryAccessor.listFiles();

        assertEquals(1, testDirectory.size());
    }

    @Test
    public void fileSystemShouldLoadSpecifiedFile() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("Maps/Trades");
        FileSystemDirectoryAccessor directoryAccessor = new FileSystemDirectoryAccessor(new File(resource.getPath()));

        ByteBuffer file = directoryAccessor.getFile("June2016.txt");

        assertNotNull(file);
    }

    @Test
    public void fileSystemShouldReturnNullForMissingFile() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("Maps/Trades");
        FileSystemDirectoryAccessor directoryAccessor = new FileSystemDirectoryAccessor(new File(resource.getPath()));

        ByteBuffer file = directoryAccessor.getFile("June2016_A.txt");

        assertNull(file);
    }
}
