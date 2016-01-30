package com.excelian.mache.chroniclemap;

import com.excelian.mache.chroniclemap.ChronicleMapMacheProvisioner.ChronicleMapBuilderConfig;
import com.excelian.mache.core.HashMapCacheLoader;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;

import static com.excelian.mache.chroniclemap.ChronicleMapMacheProvisioner.chronicleMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.util.reflection.Whitebox.getInternalState;

public class ChronicleMapMacheProvisionerShould {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldApplyLambdaConfigsAgainstBuilder() throws Throwable {
        ChronicleMapMacheProvisioner<String, Integer> provisioner = chronicleMap(String.class, Integer.class)
                .averageKeySize(10.0)
                .actualChunksPerSegment(100);

        List<ChronicleMapBuilderConfig<String, Integer>> configs =
                (List<ChronicleMapBuilderConfig<String, Integer>>) getInternalState(provisioner, "configToApply");

        ChronicleMapBuilder<String, Integer> mockBuilder = Mockito.mock(ChronicleMapBuilder.class);

        configs.stream().forEach((config) -> config.apply(mockBuilder));

        verify(mockBuilder).averageKeySize(10.0);
        verify(mockBuilder).actualChunksPerSegment(100);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCorrectlySetSizeValues() throws Throwable {
        ChronicleMapMacheProvisioner<String, Integer> provisioner = chronicleMap(String.class, Integer.class)
                .size(1000);

        assertEquals(1000, getInternalState(provisioner, "upperWatermark"));
        assertEquals(850 - 1, getInternalState(provisioner, "lowerWatermark"));
        assertEquals(925, getInternalState(provisioner, "acceptableWatermark"));

        List<ChronicleMapBuilderConfig<String, Integer>> configs =
                (List<ChronicleMapBuilderConfig<String, Integer>>) getInternalState(provisioner, "configToApply");

        ChronicleMapBuilder<String, Integer> mockBuilder = Mockito.mock(ChronicleMapBuilder.class);

        configs.stream().forEach((config) -> config.apply(mockBuilder));

        verify(mockBuilder).entries(1150);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldCreatePersistedChronicleMap() throws Throwable {
        File persistedCacheFile = folder.newFile("persistedCacheFile");

        ChronicleMapMacheProvisioner<String, Integer> provisioner =
                chronicleMap(String.class, Integer.class).persistingTo(persistedCacheFile);

        provisioner.create(String.class, Integer.class, new HashMapCacheLoader<>(Integer.class));

        assertThat(persistedCacheFile.exists(), is(true));
    }

    @Test
    public void shouldSetCleanUpThreads() throws Throwable {
        ChronicleMapMacheProvisioner<String, Integer> provisioner =
                chronicleMap(String.class, Integer.class).withRunCleanupThread();
        assertEquals(true, getInternalState(provisioner, "runCleanupThread"));
        assertEquals(false, getInternalState(provisioner, "runNewThreadForCleanup"));

        provisioner.withRunNewThreadForCleanup();
        assertEquals(false, getInternalState(provisioner, "runCleanupThread"));
        assertEquals(true, getInternalState(provisioner, "runNewThreadForCleanup"));
    }

}