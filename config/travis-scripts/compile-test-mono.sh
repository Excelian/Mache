#!/bin/bash
xbuild ./mono-example/monoExample.sln
nunit-console /home/travis/build/Excelian/Mache/mono-example/MonoExampleTests/bin/Debug/MonoExampleTests.dll
