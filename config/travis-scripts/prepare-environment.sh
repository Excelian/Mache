#!/bin/bash

sudo pip install requests

sudo apt-get -qq update
sudo apt-get -qq install -y mono-devel mono-gmcs nunit-console npm

# Node, requires npm
sudo npm cache clean -f
sudo npm install -g n
sudo n 4.2.2
