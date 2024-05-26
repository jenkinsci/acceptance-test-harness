# curl -s https://raw.githubusercontent.com/jenkinsci/docker-fixtures/master/src/main/resources/org/jenkinsci/test/acceptance/docker/fixtures/JavaContainer/Dockerfile | sha1sum | cut -c 1-12
FROM jenkins/java:978f1af53461
COPY *.pub /tmp
RUN cat /tmp/*.pub >> /home/test/.ssh/authorized_keys
