#!/bin/bash

# Usage: `eval $(./vnc.sh)`

display=":42"
if [ ! -z "$1" ]; then
  display=":$1"
fi

vncserver -kill $display > /dev/null
vncserver -geometry 1750x1250 -localhost no $display > /dev/null
if command -v vncviewer >/dev/null 2>&1; then
  vncviewer localhost$display > /dev/null &
fi

echo export BROWSER_DISPLAY=$display
if [ "$SHARED_DOCKER_SERVICE" == "true" ]; then
    # No need to use separate variable when in container
    echo export DISPLAY=$display
fi
