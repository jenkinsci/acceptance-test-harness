FROM jenkins/java:6894df180381
COPY *.pub /tmp
RUN cat /tmp/*.pub >> /home/test/.ssh/authorized_keys
