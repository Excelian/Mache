#!/bin/bash
./gradlew :mache-vertx:run &
MACHE_PID=$!
echo "Mache started, PID $MACHE_PID"

NEXT_WAIT_TIME=5
MAX_WAIT_TIME=15
until nc -z -v -w 1 localhost 8080 || [ $NEXT_WAIT_TIME -eq $MAX_WAIT_TIME ]; do
   sleep $(( NEXT_WAIT_TIME++ ))
done

if [ $NEXT_WAIT_TIME -eq $MAX_WAIT_TIME ] && nc -z -v -w 1 localhost 8080; then
   echo 'Unable to start Mache REST Service on 8080'
   exit 1
fi

echo "Running python REST tests"
python mache-example/src/examples/python/MacheRestTest.py || { echo 'Python REST test failed' ; exit 1; }

#echo "Running node REST tests"
#node mache-example/src/examples/javascript/MacheRestTest.js || { echo 'Javascript REST test failed' ; exit 1; }

echo "Running mono REST tests"
wget -P mache-example/src/examples/mono/ www.nuget.org/NuGet.exe
mozroots --import --sync
(cd mache-example/src/examples/mono/; mono NuGet.exe restore -verbosity detailed)
xbuild ./mache-example/src/examples/mono/MonoExample.sln || { echo 'Mono build failed' ; exit 1; }
nunit-console ./mache-example/src/examples/mono/MonoExampleTests/bin/Debug/MonoExampleTests.dll || { echo 'Mono REST test failed' ; exit 1; }

echo "Stopping mache"
kill $MACHE_PID

echo "REST Tests complete"