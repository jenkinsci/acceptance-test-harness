#!/bin/sh
[ -d my-webapp ] || mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-webapp -DarchetypeArtifactId=maven-archetype-webapp
cd my-webapp
mvn install
