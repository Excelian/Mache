[![Build Status](https://travis-ci.org/Excelian/Mache.svg?branch=master)](https://travis-ci.org/Excelian/Mache.svg?branch=master)

# Mache
A NearSide NoSQL Cache with eventing and other features; built from a 'mash'-up 
of open source technologies for multiple pluggable NoSQL backends and multiple 
pluggable messaging platforms.

## Features:
- Nearside caching for common NoSQL platforms using various Open Source In Memory LRU Caches
- ORM Modelling for POJO's using Spring data
- Event listener support 
- Eventing and invalidation between various client-side caches 

### Current In Memory Cache Support
- OpenHFT ChronicleMap
- Google Guava
- Caffeine

### Current Storage Layer Support
- Cassandra 
- Mongodb 
- Couchbase

### Message Queue Support
- Kafka
- Rabbit
- ActiveMQ

## Roadmap
- Continuous query caching; leveraging cache-events and implicit query support
- Local filtering


## Building the code
* The code is built using JDK 1.8 using Gradle 2+.
* There are integration tests that exercise the various supported Big Data 
  stores, these are disabled if Excelian's integration test environment `BluePrint`
  is not reachable so you will still have a passing build when you clone the 
  project.

## Documentation
The documentation is all present on the [Mache Github wiki](https://github.com/Excelian/Mache/wiki/)

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
