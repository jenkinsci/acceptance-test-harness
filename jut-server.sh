#!/bin/sh
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
if [ ! -d "$DIR/target" ]
then
  mvn package -DskipTests -f "$DIR/pom.xml"
fi
sh "$DIR/target/appassembler/bin/jut-server" "$@"
