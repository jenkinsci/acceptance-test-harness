#!/usr/bin/env bash

# Usage: `eval $(./vnc.sh)`

display=':42'
if [[ -n $1 ]]; then
	display=":$1"
fi

# Use setsid to detach processes that are supposed to keep running (vncserver and vncviewer) from current process group,
# so ^C sent to any command run afterwards does not kill the vnc. Happens commonly when ATH is paused by INTERACTIVE=true
# and then killed normally.

vncserver -kill "$display" >/dev/null
setsid vncserver "$display" -geometry 1750x1250 1>&2
if command -v vncviewer >/dev/null 2>&1; then
	setsid vncviewer "localhost$display" >/dev/null &
fi

echo "export BROWSER_DISPLAY=$display"
if [[ $SHARED_DOCKER_SERVICE == "true" ]]; then
	# No need to use separate variable when in container
	echo "export DISPLAY=$display"
fi
