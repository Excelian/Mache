#!/bin/bash

echo "Installing requests for python"
pip install --user requests || { echo 'pip package install failed'; exit 1; }

echo "Installing n for node"
# Possible issue with https certificate
sudo npm config set registry http://registry.npmjs.org/
sudo npm cache clean -f
sudo npm install -g n

echo "Installing node version 4.2.2"
sudo -E n 4.2.2
