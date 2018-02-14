FROM jenkins/java:6894df180381

RUN cd /tmp && \
    wget -nv -O - https://get.docker.com/builds/Linux/x86_64/docker-1.13.1.tgz | tar xvfz - docker/docker && \
    chmod a+x docker/docker && \
    mv docker/docker /usr/bin/docker
VOLUME /home/test/workspace
COPY entrypoint.sh /usr/bin/entrypoint.sh
ENTRYPOINT ["/bin/sh", "/usr/bin/entrypoint.sh"]
