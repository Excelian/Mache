package com.excelian.mache.examples.couchbase;

import com.excelian.mache.builder.Builder;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.excelian.mache.builder.Builder.mache;
import static com.excelian.mache.builder.Builder.namedCluster;
import static com.excelian.mache.builder.Builder.server;

/**
 * A factory for a Couchbase backed {@link Example}.
 */
public class CouchbaseExample implements Example<CouchbaseAnnotatedMessage> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Override
    public Mache<String, CouchbaseAnnotatedMessage> exampleCache() {
        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
        return mache()
            .backedByCouchbase()
            .at(server("10.28.1.140", 8091))
            .with(namedCluster("BluePrint"))
            .withKeyspace(keySpace)
            .toStore(CouchbaseAnnotatedMessage.class)
            .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
            .withNoMessaging().macheUp();
    }
}
