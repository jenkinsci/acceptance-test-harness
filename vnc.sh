#!/bin/bash

display=":42"
if [ ! -z "$1" ]; then
  display=":$1"
fi

vncserver $display
vncviewer $display &

export BROWSER_DISPLAY=$display
