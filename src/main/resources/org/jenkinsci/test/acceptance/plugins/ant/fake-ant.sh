#!/bin/sh
echo fake ant at $0
export ANT_HOME=
exec "{0}/ant" "$@"
