package com.excelian.mache.jmeter.mongo.knownKeys;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import java.util.List;

/**
 * Created by jbowkett on 11/09/2015.
 */
public class ShuffledSequenceTest {

    private static int MAX = 100;
    private final ShuffledSequence shuffledSequence = new ShuffledSequence();

    @Test
    public void testArrayIsSameSizeAsGiven() {
        final List<Integer> shuffled = shuffledSequence.upTo(MAX);
        assertEquals(MAX, shuffled.size());
    }

    @Test
    public void testArrayIsShuffled() {
        final List<Integer> shuffled = shuffledSequence.upTo(MAX);
        assertNotEquals(0, shuffled.get(0));
        assertNotEquals(MAX - 1, shuffled.get(MAX - 1));
        assertNotEquals(MAX - 2, shuffled.get(MAX - 2));
    }

    @Test
    public void testOnlyIntegersUpToSizeArePresent() {
        final List<Integer> shuffled = shuffledSequence.upTo(MAX);
        for (int i = 0; i < MAX; i++) {
            assertTrue(String.format("Shuffled list does not contain [%d]", i), shuffled.contains(i));
        }
    }

    private void assertNotEquals(Integer expected, Integer actual) {
        org.junit.Assert.assertNotEquals(expected, actual);
    }
}
