#!/bin/bash
xbuild ./mono-example/monoExample.sln
nunit-console ./mono-example/MonoExampleTests/bin/Debug/MonoExampleTests.dll
