#!/bin/bash
CMD="target/appassembler/bin/jut-server"
if [ ! -s $CMD ]
then
  mvn package -DskipTests
fi
sh "$CMD" "$@"
