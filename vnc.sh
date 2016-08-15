#!/bin/bash

# $ eval $(./vnc.sh)

display=":42"
if [ ! -z "$1" ]; then
  display=":$1"
fi

vncserver -kill $display
vncserver -geometry 1750x1250 $display > /dev/null
(vncviewer $display || vncviewer localhost$display) > /dev/null &

echo export BROWSER_DISPLAY=$display
