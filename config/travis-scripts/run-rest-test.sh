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

echo "Mache started, PID $MACHE_PID"

echo "Running python REST tests"
python mache-example/src/examples/python/MacheRestTest.py || { echo 'Python REST test failed' ; exit 1; }

#echo "Running node REST tests"
#node mache-example/src/examples/javascript/MacheRestTest.js || { echo 'Javascript REST test failed' ; exit 1; }

echo "Running mono REST tests"
wget -P mache-example/src/examples/mono/ https://dist.nuget.org/win-x86-commandline/latest/nuget.exe
mono mache-example/src/examples/mono/nuget.exe restore mache-example/src/examples/mono/
xbuild ./mache-example/src/examples/mono/MonoExample.sln || { echo 'Mono build failed' ; exit 1; }
nunit-console ./mache-example/src/examples/mono/MonoExampleTests/bin/Debug/MonoExampleTests.dll || { echo 'Mono REST test failed' ; exit 1; }

echo "Stopping mache"
kill $MACHE_PID

echo "REST Tests complete"