#!/bin/bash
gradle :mache-vertx:installApp
./mache-vertx/build/install/mache-vertx/bin/mache-vertx &
MACHE_PID=$!

NEXT_WAIT_TIME=0
until nc -z -v -w 1 localhost 8080 || [ $NEXT_WAIT_TIME -eq 5 ]; do
   sleep $(( NEXT_WAIT_TIME++ ))
done

if [ $NEXT_WAIT_TIME -eq 5 ] && [ $? -ne 0 ]; then
   echo 'unable to start vertx on 8080'
   exit 1
fi

echo "Mache started, PID $MACHE-PID"

python mache-example/src/examples/python/MacheRestTest.py || { echo 'Python REST test failed' ; exit 1; }

kill $MACHE_PID