#!/bin/bash

# $ eval $(./vnc.sh)

display=":42"
if [ ! -z "$1" ]; then
  display=":$1"
fi

vncserver $display > /dev/null
vncviewer $display > /dev/null &

echo export BROWSER_DISPLAY=$display
