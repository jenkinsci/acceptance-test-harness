FROM jenkins/java:e3665dc1a362
COPY *.pub /tmp
RUN cat /tmp/*.pub >> /home/test/.ssh/authorized_keys
