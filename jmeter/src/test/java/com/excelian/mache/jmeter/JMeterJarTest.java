package com.excelian.mache.jmeter;

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
    public void JmeterMachePluginJarShouldBeBuilt(){

        File f = new File(MAIN_PLUGIN_JAR_PATH);
        assertTrue("Expected the plugin jar to have been built at location "+f.getAbsolutePath(), f.exists());
    }

    @Test
    public void JmeterMachePluginJarMustContainAManifest() throws IOException {

        JarFile jarFile = new JarFile(MAIN_PLUGIN_JAR_PATH);
        assertNotNull(jarFile.getJarEntry("META-INF/MANIFEST.MF"));
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

            assertFalse("Jar should not contain mache code but contains "+entryName, entryName.contains("mache"));
        }
    }
}
