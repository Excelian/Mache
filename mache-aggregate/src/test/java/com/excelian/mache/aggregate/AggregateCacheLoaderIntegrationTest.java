package com.excelian.mache.aggregate;

import com.excelian.mache.core.Mache;
import org.junit.Test;

import static com.excelian.mache.aggregate.builder.AggregateProvisioner.multipleStores;
import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.file.builder.FileProvisioner.file;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Objects;

public class AggregateCacheLoaderIntegrationTest {

    @Test
    public void setup() throws Exception {
        final Mache<String, TestEntity> cache =
            mache(String.class, TestEntity.class)
                .backedBy(
                    multipleStores(
                        file(),
                        file()
                    )
                )
                .withNoMessaging()
                .macheUp();
        assertNotNull(cache);
    }


    public static class TestEntity implements Serializable {
        String key;

        String type;

        double value;

        public TestEntity(String key, String type, double value) {
            this.key = key;
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            TestEntity that = (TestEntity) other;
            return Objects.equals(value, that.value)
                && Objects.equals(key, that.key)
                && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, type, value);
        }
    }
}

