FROM jenkins/java:fb8985ac2d90
COPY *.pub /tmp
RUN cat /tmp/*.pub >> /home/test/.ssh/authorized_keys
