#!/bin/bash

CB_BINARY_URL="http://packages.couchbase.com/releases/3.1.0/couchbase-server-enterprise_3.1.0-ubuntu12.04_amd64.deb"

if [ ! -d ${HOME}/binaries ] ; then
  mkdir ~/binaries
fi

if [ ! -e $HOME/binaries/couchbase.deb ] ; then
  wget ${CB_BINARY_URL} -O ${HOME}/binaries/couchbase.deb
fi

if [ -d $HOME/opt/couchbase ] ; then
  rm -rf $HOME/opt/couchbase
fi

dpkg-deb -x $HOME/binaries/couchbase.deb $HOME

cd $HOME/opt/couchbase
./bin/install/reloc.sh `pwd`
./bin/couchbase-server -- -noinput -detached
sleep 20
./bin/couchbase-cli cluster-init -c 127.0.0.1:8091  --cluster-init-username=Administrator --cluster-init-password=password --cluster-init-port=8091 --cluster-init-ramsize=1024
./bin/couchbase-cli bucket-create -c 127.0.0.1:8091 --bucket=test --bucket-type=couchbase --bucket-port=11211 --bucket-ramsize=512  --bucket-replica=1 -u Administrator -p password
