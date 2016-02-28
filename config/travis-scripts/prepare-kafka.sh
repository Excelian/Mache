#!/bin/bash

KAFKA_BINARY_URL="http://www.apache.org/dist/kafka/0.8.2.1/kafka_2.10-0.8.2.1.tgz"

if [ ! -d $HOME/binaries ] ; then
  mkdir ~/binaries
fi

if [ ! -e $HOME/binaries/kafka.tgz ] ; then
  wget $KAFKA_BINARY_URL -O $HOME/binaries/kafka.tgz
fi

if [ -d /tmp/kafka ] ; then
  rm -rf /tmp/kafka
fi
if [ -d /tmp/zookeeper ] ; then
  rm -rf /tmp/zookeeper
fi
if [ -d /tmp/kafka-logs ] ; then
  rm -rf /tmp/kafka-logs
fi

mkdir -p /tmp/kafka && tar xzf $HOME/binaries/kafka.tgz -C /tmp/kafka --strip-components 1
nohup bash -c "cd /tmp/kafka && bin/zookeeper-server-start.sh config/zookeeper.properties &"
nohup bash -c "cd /tmp/kafka && bin/kafka-server-start.sh config/server.properties &"
sleep 5
/tmp/kafka/bin/kafka-topics.sh --create --partitions 1 --replication-factor 1 --topic com.excelian.mache.integrations.eventing.TestEventingBase.TestEntity --zookeeper localhost:2181
/tmp/kafka/bin/kafka-topics.sh --create --partitions 1 --replication-factor 1 --topic com.excelian.mache.integrations.eventing.TestEventingBase.TestOtherEntity --zookeeper localhost:2181
