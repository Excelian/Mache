package com.excelian.mache.directory.loader;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JsonDirectoryCacheLoaderTest {

    private JsonDirectoryCacheLoader jsonLoader;

    @Before
    public void setup() {
        URL resource = this.getClass().getClassLoader().getResource("Maps/Trades");
        FileSystemDirectoryAccessor directoryAccessor = new FileSystemDirectoryAccessor(new File(resource.getPath()));
        DirectoryCacheLoader directoryCacheLoader = new DirectoryCacheLoader(directoryAccessor);
        jsonLoader = new JsonDirectoryCacheLoader(directoryCacheLoader);
    }

    @Test
    public void loaderShouldReturnJsonFile() throws Exception {
        String loaded = jsonLoader.load("June2016.txt");

        assertEquals("{ \"payload\": \"MDEvMDYvMjAxNiwxLEIsMTE0LjU2LDIsMTEyNTE=\" }", loaded);
    }

    @Test
    public void loaderShouldReturnNullMissingFile() throws Exception {
        String loaded = jsonLoader.load("not_a_file.txt");

        assertNull(loaded);
    }
}