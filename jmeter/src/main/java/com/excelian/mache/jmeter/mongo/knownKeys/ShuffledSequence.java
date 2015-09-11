package com.excelian.mache.jmeter.mongo.knownKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jbowkett on 11/09/2015.
 */
public class ShuffledSequence {
    public List<Integer> upTo(int size) {
        final List<Integer> ints = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ints.add(i, i);
        }
        Collections.shuffle(ints);
        return ints;
    }
}
