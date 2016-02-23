package com.excelian.mache.directory.loader;

import org.junit.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

public class DirectoryCacheLoaderTests {

    @Test
    public void loaderShouldCallFileSystemOnLoad() throws Exception {
        DirectoryAccessor mock = mock(DirectoryAccessor.class);
        DirectoryCacheLoader loader = new DirectoryCacheLoader(mock);

        loader.load("ABC");

        verify(mock).getFile("ABC");
    }
}
