#!/bin/bash

cd ~/build/Excelian/Mache
git checkout $1
git pull
travis compile > ~/build/build.sh && chmod 755 ~/build/build.sh
cd ~/build
