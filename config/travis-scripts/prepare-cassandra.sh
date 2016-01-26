#!/bin/bash

CASSANDRA_BINARY_URL="https://archive.apache.org/dist/cassandra/2.2.4/apache-cassandra-2.2.4-bin.tar.gz"

if [ ! -d $HOME/binaries ] ; then
  mkdir ~/binaries
fi

if [ ! -e $HOME/binaries/kafka.tgz ] ; then
  wget $CASSANDRA_BINARY_URL -O $HOME/binaries/cassandra.tgz
fi

mkdir -p cassandra && tar xzf $HOME/binaries/cassandra.tgz -C cassandra --strip-components 1

nohup bash -c "cd cassandra && bin/cassandra & "
sleep 5
