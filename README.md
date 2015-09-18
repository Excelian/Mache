[![Build Status](https://travis-ci.org/Excelian/Mache.svg?branch=master)](https://travis-ci.org/Excelian/Mache.svg?branch=master)

# Mache
A NearSide NoSQL Cache with eventing and other features; built from a 'mash'-up 
of open source technologies for multiple pluggable NoSQL backends and multiple 
pluggable messaging platforms.

## Features:
- Nearside caching for common NoSQL platforms using Google's Guava Cache 
- ORM Modelling for POJO's using Spring data
- Event listener support 
- Eventing and invalidation between various client-side caches 

## Current Storage Layer Support
- Cassandra 
- Mongodb 
- Couchbase

## Message Queue Support
- Kafka
- Rabbit
- ActiveMQ

## Roadmap
- Continuous query caching; leveraging cache-events and implicit query support
- JSON support 
- Local filtering

## Getting started:

 1. Add mache-core.jar to the classpath
 2. Add the chosen data store provider jar to the classpath; mache-cassandra.jar, 
    mache-couchbase.jar or mache-mongo.jar
 3. If you require eventing, add mache-observable.jar to the classpath, then add 
    the suitable messaging provider; mache-activemq.jar, mache-kafka.jar or 
    mache-rabbit.jar

The released Mache artifacts are housed at jcenter, so for instance if you wanted 
a Mache backed by Cassandra leveraging ActiveMQ for the eventing, you will require 
the following in your build.gradle:
```` gradle
repositories {
    jcenter()
}
dependencies {
  compile 'com.excelian.mache:mache-core:1.0'
  compile 'com.excelian.mache:mache-cassandra:1.0'
  compile 'com.excelian.mache:mache-observable:1.0'
  compile 'com.excelian.mache:mache-activemq:1.0'
````

Once you have the appropriate jars in your classpath you can then construct a 
Mache using the builder interface.  This will present you with appropriate 
choices given the service providers you have installed.  Examples of how these 
are constructed can be found in the `mache-example` component
* `mache-example/src/examples/java/com/excelian/mache/examples/cassandra/CassandraExample.java`
* `mache-example/src/examples/java/com/excelian/mache/examples/couchbase/CouchbaseExample.java`
* `mache-example/src/examples/java/com/excelian/mache/examples/mongo/MongoExample.java`


```` java
@Document
public static class TestEntity {
    @Id
    String pkString;
    
    private int firstInt = 1;
    
    @Field(value = "differentName")
    private double aDouble = 1.0;
    
    @Indexed
    private String aString = "some-other-attribute";
    
    public TestEntity(String pkString) {
        this.pkString = pkString;
    }
    
    public static void main(String [] args){
        final String keySpace = "my_mongo_keyspace_name";        
        Mache<String, TestEntity> mache = mache(String.class, TestEntity.class)
                        .backedBy(mongodb()
                                .withSeeds(new ServerAddress("10.28.1.140", 9042))
                                .withDatabase(keySpace)
                                .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                                .build())
                        .withNoMessaging()
                        .macheUp()
        mache.put("test-1", new TestEntity("some-value-to-store"));
        TestEntity retrieved = mache.get("test-1");
        assertEquals("some-value-to-store", retrieved.pkString);
    }
}
````


## Building the code
* The code is built using JDK 1.8 using Gradle 2+.
* There are integration tests that exercise the various supported Big Data 
  stores, these are disabled if Excelian's integration test environment `BluePrint`
  is not reachable so you will still have a passing build when you clone the 
  project.


# Get Involved!

mailto:mache@excelian.com

## Team board
https://trello.com/b/DYSHTr8c/excelian-mache

## Further Reading
https://github.com/infinispan/infinispan/wiki/Near-Caching
 
## Acknowledgements:
* Google Guava Cache for flexible caching implementation 
   https://code.google.com/p/guava-libraries/wiki/CachesExplained
* Spring Data wrappers to map POJO's to each data platform 
   http://projects.spring.io/spring-data/
* http://www.codeaffine.com/2013/11/18/a-junit-rule-to-conditionally-ignore-tests/
 for Junit conditional ignore
