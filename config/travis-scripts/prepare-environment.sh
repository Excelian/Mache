#!/bin/bash
sudo npm cache clean -f
sudo npm install -g n
sudo n 4.2.2

sudo pip install requests

sudo apt-get -qq update
sudo apt-get -qq install -y mono-devel mono-gmcs nunit-console
