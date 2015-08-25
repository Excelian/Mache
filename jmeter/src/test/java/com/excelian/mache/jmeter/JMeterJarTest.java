package com.excelian.mache.jmeter;

import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.events.integration.KafkaMQFactory;
import com.excelian.mache.events.integration.RabbitMQFactory;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.excelian.mache.observable.ObservableCacheFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.*;

public class JMeterJarTest {

    public static final String MAIN_PLUGIN_JAR_PATH = "./build/libs/jmeter-mache-plugin-0.1-SNAPSHOT.jar";
    public static final String SUPPORT_JAR_PATH = "./build/distributions/jmeter-mache-plugin-support-all-0.1-SNAPSHOT-src.zip";


    @Test
    public void JmeterMachePluginDirectoryExists(){

        File f = new File(new File(MAIN_PLUGIN_JAR_PATH).getParent());
        assertTrue("Expected the plugin directory to exist "+f.getAbsolutePath(), f.exists());
    }

    @Test
    public void JmeterMachePluginJarShouldBeBuilt(){

        File f = new File(MAIN_PLUGIN_JAR_PATH);
        assertTrue("Expected the plugin jar to have been built at location "+f.getAbsolutePath(), f.exists());
    }

    @Test
    public void JmeterMachePluginJarMustContainAManifest() throws IOException {

        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);
        assertFileIsPresentWithinJar(jarFile, "META-INF/MANIFEST.MF");
    }

    @Test
    public void JmeterMachePluginJarContainsTheExpectedCoreFiles() throws IOException {
        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);

        assertFileIsPresentWithinJar(jarFile, MacheAbstractJavaSamplerClient.class);
        assertFileIsPresentWithinJar(jarFile, Mache.class);
        assertFileIsPresentWithinJar(jarFile, ObservableCacheFactory.class);
    }

    @Test
    public void JmeterMachePluginJarContainsTheDatabaseIntegrationFiles() throws IOException {
        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);

        assertFileIsPresentWithinJar(jarFile, CassandraCacheLoader.class);
        assertFileIsPresentWithinJar(jarFile,MongoDBCacheLoader.class);
    }

    @Test
    public void JmeterMachePluginJarContainsTheExpectedMessagingIntegrationClasses() throws IOException {
        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);

        assertFileIsPresentWithinJar(jarFile, ActiveMQFactory.class);
        assertFileIsPresentWithinJar(jarFile, RabbitMQFactory.class);
        assertFileIsPresentWithinJar(jarFile, KafkaMQFactory.class);
    }

    private void assertFileIsPresentWithinJar(JarFile jarFile, String filePath)
    {
        assertNotNull("Expected " + filePath + " to be present within " + jarFile.getName(), jarFile.getJarEntry(filePath));
    }

    private void assertFileIsPresentWithinJar(JarFile jarFile, Class cls)
    {
        String filePath = cls.getCanonicalName().replace('.', '/');
        assertFileIsPresentWithinJar(jarFile, filePath + ".class");
    }

    @Test
    public void JmeterMachePluginSupportDirectoryExists(){

        File f = new File(new File(SUPPORT_JAR_PATH).getParent());
        assertTrue("Expected the plugin directory to exist " + f.getAbsolutePath(), f.exists());
    }

    @Test
    public void JmeterMacheSupportJarShouldBeBuilt(){

        File f = new File(SUPPORT_JAR_PATH);
        assertTrue("Expected the plugin support jar to have been built at location "+f.getAbsolutePath(), f.exists());
    }

    @Test
    public void TheJmeterMacheSupportJarShouldNotContainMacheJars() throws IOException {

        File f = new File(SUPPORT_JAR_PATH);
        JarFile jarFile = new JarFile(f.getAbsolutePath());

        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String entryName = entry.getName();

            assertFalse("Jar should not contain mache code but contains " + entryName, entryName.contains("mache"));
        }
    }
}
