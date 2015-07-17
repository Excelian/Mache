# Mache
A NearSide NoSQL Cache with eventing and other features; built from a 'mash'-up of OpenSource technologies for multiple NoSQL backends

# Team board
https://trello.com/b/DYSHTr8c/excelian-mache

## Features:
- Nearside caching for common NoSQL platforms using Google's Guava Cache. 
- ORM Modelling for POJO's using Spring data
- JSON support (*)
- Eventing and invalidation between various client-side caches (*) (Kafka, Rabbit etc)
- Event listener support (*)
- Continuous query caching; leveraging cache-events and implicit query support (*)

\* coming soon ;)

## Storage Layer Support
- Cassandra (done)
- Mongodb (done)
- Couchbase

# Roadmap
- V1 Nearside Caching
- V2 Nearside with remote invalidation

## Leverages:
- Google Guava Cache for flexible caching implementation 
   https://code.google.com/p/guava-libraries/wiki/CachesExplained
- Spring Data wrappers to map POJO's to each data platform 
   http://projects.spring.io/spring-data/

## Getting started:
- Take a look at the integration tests for each data platform (mongodb shown below)

```
 @Document
    public static class TestEntity {
        @Id
        String pkString = "yay";

        private int firstInt = 1;

        @Field(value = "differentName")
        private double aDouble = 1.0;

        @Indexed
        private String aString = "yay";


        public TestEntity(String pkString) {
            this.pkString = pkString;
        }
    }
  List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress("10.28.1.140", 27017));
  CacheThing cacheThing = new CacheThing<>(
                new MongoDBCacheLoader<String,TestEntity>(TestEntity.class, serverAddresses, true, keySpace));)
   cacheThing.put("test-1", new TestEntity("value-yay"));
   TestEntity test = cacheThing.get("test-1");
    assertEquals("value-yay", test.pkString);
```
## Future work
- Eventing mechanism
- JSON

## Interesting links
https://github.com/infinispan/infinispan/wiki/Near-Caching

## Running the tests

### Pre-requisites
You will need to install Rabbit MQ.  
Rabbit server will need to be running while you run the unit tests.  
Run with: `rabbitmq-server &`

The following tests fail when run outside of Excelian's test cluster:
 *
 *
 *
 

