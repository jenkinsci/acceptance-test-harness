# curl -s https://raw.githubusercontent.com/jenkinsci/docker-fixtures/master/src/main/resources/org/jenkinsci/test/acceptance/docker/fixtures/JavaContainer/Dockerfile | sha1sum | cut -c 1-12
FROM jenkins/java:387404da3ce7

RUN cd /tmp && \
    wget -nv -O - https://get.docker.com/builds/Linux/x86_64/docker-1.13.1.tgz | tar xvfz - docker/docker && \
    chmod a+x docker/docker && \
    mv docker/docker /usr/bin/docker
VOLUME /home/test/workspace
COPY entrypoint.sh /usr/bin/entrypoint.sh
ENTRYPOINT ["/bin/sh", "/usr/bin/entrypoint.sh"]
