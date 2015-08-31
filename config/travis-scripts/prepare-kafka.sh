#!/bin/bash

wget http://www.apache.org/dist/kafka/0.8.2.1/kafka_2.10-0.8.2.1.tgz -O kafka.tgz
mkdir -p kafka && tar xzf kafka.tgz -C kafka --strip-components 1
nohup bash -c "cd kafka && bin/zookeeper-server-start.sh config/zookeeper.properties &"
nohup bash -c "cd kafka && bin/kafka-server-start.sh config/server.properties &"
sleep 5
kafka/bin/kafka-topics.sh --create --partitions 1 --replication-factor 1 --topic com.excelian.mache.integrations.eventing.TestEventingBase.TestOtherEntity --zookeeper localhost:2181
