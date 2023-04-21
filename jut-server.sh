#!/usr/bin/env bash
DIR="$(cd "$(dirname "$0")" && pwd)"
CMD="$DIR/target/appassembler/bin/jut-server"
if [[ ! -s $CMD ]]; then
	mvn package -DskipTests -f "$DIR/pom.xml"
fi
sh "$CMD" "$@"
