package com.excelian.mache.hadoop;

import com.codeaffine.test.ConditionalIgnoreRule;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Hadoop.
 * To run in development I have used a Docker container (sequenceiq/hadoop-docker)
 * But with port 9000 exposed & hadoop security disabled in hdfs-site.xml
 */
@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningHadoopForTests.class)
public class HadoopDirectoryAccessorTest {

    public static final String HDFS_SERVER = "hdfs://" + new NoRunningHadoopForTests().getHost() + ":9000";
    public static final String ROOT_DIRECTORY = "test/file/";

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private HadoopDirectoryAccessor hadoopDirectoryAccessor;

    @Before
    public void setup() {
        Configuration configuration = new Configuration();
        configuration.set("fs.default.name", HDFS_SERVER);
        Path path = new Path(ROOT_DIRECTORY);
        hadoopDirectoryAccessor = new HadoopDirectoryAccessor(configuration, path);

        try {
            FileSystem fileSystem = FileSystem.get(configuration);
            fileSystem.mkdirs(new Path(ROOT_DIRECTORY));
            fileSystem.createNewFile(new Path(ROOT_DIRECTORY + "test.txt"));
            fileSystem.deleteOnExit(new Path(ROOT_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void hadoopShouldReturnKnownFile() throws Exception {
        ByteBuffer file = hadoopDirectoryAccessor.getFile("test.txt");

        assertNotNull(file);
    }

    @Test
    public void hadoopShouldListFiles() throws Exception {
        List<String> files = hadoopDirectoryAccessor.listFiles();

        assertEquals(1, files.size());
        assertTrue(files.get(0).toLowerCase().contains("test.txt"));
    }
}