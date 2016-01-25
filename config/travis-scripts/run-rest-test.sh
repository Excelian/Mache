#!/bin/bash
gradle :mache-vertx:installApp
nohup ./mache-vertx/build/install/mache-vertx/bin/mache-vertx &
MACHE-PID=$!
echo 'Mache started, PID $MACHE-PID'
nc -z -v -w 10 localhost 8080 || { echo 'unable to start vertx on 8080' ; exit 1; }

python mache-example/src/examples/python/MacheRestTest.py || { echo 'Python REST test failed' ; exit 1; }

kill $MACHE-PID