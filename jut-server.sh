#!/bin/sh
if [ ! -d target ]
then
  mvn package -DskipTests
fi
sh target/appassembler/bin/jut-server "$@"
