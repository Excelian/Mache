package com.excelian.mache.jmeter;

import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.events.integration.KafkaMQFactory;
import com.excelian.mache.events.integration.RabbitMQFactory;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.excelian.mache.observable.ObservableCacheFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.*;

public class JMeterJarTest {

    private static final Logger LOG = LoggerFactory.getLogger(JMeterJarTest.class);

    public static final String MAIN_PLUGIN_JAR_PATH = "./build/libs/jmeter-mache-plugin-0.6.jar";
    public static final String SUPPORT_JAR_PATH =
        "./build/distributions/jmeter-mache-plugin-support-all-0.6-src.zip";

    @Test
    public void jmeterMachePluginDirectoryExists() {

        File f = new File(new File(MAIN_PLUGIN_JAR_PATH).getParent());
        assertTrue("Expected the plugin directory to exist " + f.getAbsolutePath(), f.exists());
    }

    @Test
    public void jmeterMachePluginJarShouldBeBuilt() {

        File f = new File(MAIN_PLUGIN_JAR_PATH);
        assertTrue("Expected the plugin jar to have been built at location " + f.getAbsolutePath(), f.exists());
    }

    @Test
    public void jmeterMachePluginJarMustContainAManifest() throws IOException {

        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);
        assertItemIsPresentWithinJar(jarFile, "META-INF/MANIFEST.MF");
    }

    @Test
    public void jmeterMachePluginJarContainsTheExpectedCoreFiles() throws IOException {
        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);

        assertItemIsPresentWithinJar(jarFile, MacheAbstractJavaSamplerClient.class);
        assertItemIsPresentWithinJar(jarFile, Mache.class);
        assertItemIsPresentWithinJar(jarFile, ObservableCacheFactory.class);
    }

    @Test
    public void jmeterMachePluginJarContainsTheDatabaseIntegrationFiles() throws IOException {
        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);

        assertItemIsPresentWithinJar(jarFile, CassandraCacheLoader.class);
        assertItemIsPresentWithinJar(jarFile, MongoDBCacheLoader.class);
    }

    @Test
    public void jmeterMachePluginJarContainsTheExpectedMessagingIntegrationClasses() throws IOException {
        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);

        assertItemIsPresentWithinJar(jarFile, ActiveMQFactory.class);
        assertItemIsPresentWithinJar(jarFile, RabbitMQFactory.class);
        assertItemIsPresentWithinJar(jarFile, KafkaMQFactory.class);
    }

    @Test
    public void jmeterMachePluginSupportDirectoryExists() {

        File f = new File(new File(SUPPORT_JAR_PATH).getParent());
        assertTrue("Expected the plugin directory to exist " + f.getAbsolutePath(), f.exists());
    }

    @Test
    public void jmeterMacheSupportJarShouldBeBuilt() {

        File f = new File(SUPPORT_JAR_PATH);
        assertTrue("Expected the plugin support jar to have been built at location " + f.getAbsolutePath(), f.exists());
    }

    @Test
    public void jmeterMacheSupportJarShouldNotBeEmpty() throws IOException {

        File f = new File(SUPPORT_JAR_PATH);
        JarFile jarFile = new JarFile(f.getAbsolutePath());
        assertTrue(jarFile.size() > 0);
    }

    @Test
    public void theJmeterMacheSupportJarShouldNotContainMacheJars() throws IOException {

        File f = new File(SUPPORT_JAR_PATH);
        JarFile jarFile = new JarFile(f.getAbsolutePath());

        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String entryName = entry.getName();

            assertFalse("Jar should not contain mache code but contains " + entryName, entryName.contains("mache"));
        }
    }

    private void assertItemIsPresentWithinJar(JarFile jarFile, String filePath) {
        assertNotNull("Expected " + filePath + " to be present within " + jarFile.getName(),
                jarFile.getJarEntry(filePath));
    }

    private void assertItemIsPresentWithinJar(JarFile jarFile, Class cls) {
        String filePath = cls.getCanonicalName().replace('.', '/');
        assertItemIsPresentWithinJar(jarFile, filePath + ".class");
    }

    @Test
    @Ignore
    public void dumpDirectoryTreeToDebugTravis() {
        printFnames(".");
    }

    public void printFnames(String sDir) {
        File[] faFiles = new File(sDir).listFiles();
        if (faFiles != null) {
            for (File file : faFiles) {
                if (file.getName().matches("^(.*?)")) {
                    LOG.info(file.getAbsolutePath());
                }
                if (file.isDirectory()) {
                    printFnames(file.getAbsolutePath());
                }
            }
        }
    }
}
