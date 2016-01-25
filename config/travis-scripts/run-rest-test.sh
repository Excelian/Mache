#!/bin/bash
gradle :mache-vertx:installApp
./mache-vertx/build/install/mache-vertx/bin/mache-vertx
nc -z -v -w5 localhost 8080 || { echo 'unable to start vertx on 8080' ; exit 1; }

python mache-example/src/examples/python/MacheRestTest.py || { echo 'Python REST test failed' ; exit 1; }

