package com.excelian.mache.directory.loader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utilities for manipulating/loadign a ByteBuffer.
 */
public class ByteBufferUtilities {

    /**
     * Loads a ByteBuffer from a given input stream
     * @param inputStream The input stream
     * @return Loaded bytebuffer
     * @throws Exception
     */
    public static ByteBuffer readInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int read;
        byte[] data = new byte[(int) Math.pow(2, 14)];

        while ((read = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }
        buffer.flush();

        return ByteBuffer.wrap(buffer.toByteArray());
    }
}
