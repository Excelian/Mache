#!/bin/bash

echo "Installing requests for python"
pip install requests

echo "apt-get for mono/nunit/npm packages"
sudo apt-get -qq update
sudo apt-get -qq install -y mono-devel mono-gmcs nunit-console npm

echo "Installing n for node"
# Possible issue with https certificate
sudo npm config set registry http://registry.npmjs.org/
sudo npm cache clean -f
sudo npm install -g n

echo "Installing node version 4.2.2"
sudo -E n 4.2.2
