# curl -s https://raw.githubusercontent.com/jenkinsci/docker-fixtures/master/src/main/resources/org/jenkinsci/test/acceptance/docker/fixtures/JavaContainer/Dockerfile | sha1sum | cut -c 1-12
FROM jenkins/java:132f41d4b1a9
COPY *.pub /tmp
RUN cat /tmp/*.pub >> /home/test/.ssh/authorized_keys
