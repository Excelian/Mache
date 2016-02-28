#!/bin/bash

echo "Installing requests for python"
pip install --user requests || { echo 'pip package install failed'; exit 1; }
