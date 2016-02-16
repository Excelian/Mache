#!/bin/bash

echo "Installing requests for python"
pip install --user requests || { echo 'pip package install failed'; exit 1; }

echo "Installing fakes3 for ruby"
gem install fakes3
fakes3 -r . -p 4567 &