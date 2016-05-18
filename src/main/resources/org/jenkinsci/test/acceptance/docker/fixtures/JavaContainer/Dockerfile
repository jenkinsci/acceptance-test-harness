#
# Container for running jenkins.war
#

# TODO have to inline SshdContainer/Dockerfile since 'FROM jenkins/sshd' does not work: jenkins/sshd is not pushed to the Docker registry, so we cannot build against it
FROM ubuntu
RUN apt-get update && apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd
RUN useradd test -d /home/test && \
    mkdir -p /home/test/.ssh && \
    echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDzpxmTW9mH87DMkMSqBrSecoSHVCkKbW5IOO+4unak8M8cyn+b0iX07xkBn4hUJRfKA7ezUG8EX9ru5VinteqMOJOPknCuzmUS2Xj/WJdcq3BukBxuyiIRoUOXsCZzilR/DOyNqpjjI3iNb4los5//4aoKPCmLInFnQ3Y42VaimH1298ckEr4tRxsoipsEAANPXZ3p48gGwOf1hp56bTFImvATNwxMViPpqyKcyVaA7tXCBnEk/GEwb6MiroyHbS0VvBz9cZOpJv+8yQnyLndGdibk+hPbGp5iVAIsm28FEF+4FvlYlpBwq9OYuhOCREJvH9CxDMhbOXgwKPno9GyN kohsuke@atlas' > /home/test/.ssh/authorized_keys && \
    echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDagNSCDst/8z5oH9S5QWr+QNdx+haImY0FD3IQvKdD+eWI9zUbBgtoo/yYEbLvpTWiKsgT3Hw1F8mZ+/bd2Uv3lPyoG+TSzrHL4gSal6d1RWVjCOzSosciXVm4gRUvJjKXzaz8dOg+ii9yIrbeONNK0nlDUCAKy5YXSEl0avcPdUDyR3cStL6870SyanxAzktDw0n8xMq4F/alF3PZ002bcZJrmDeNVAwkP+uO2Tf8pN37SU+nApotZmlmZR32xYHnx+/OiQ7gOAVYmgNRMg0Kwh6Q73FcY3ZWCeNHwLnr95LoEAdj3On8Qr62VhGThuQNVCqBc6SeYjArfjijpcW9 jenkins-ci@localhost' >> /home/test/.ssh/authorized_keys && \
    chown -R test:test /home/test && \
    chmod 0600 /home/test/.ssh/authorized_keys && \
    echo "test:test" | chpasswd
ENTRYPOINT ["/usr/sbin/sshd", "-D", "-e"]

# JDK is from Universe
RUN echo deb http://archive.ubuntu.com/ubuntu precise universe >> /etc/apt/sources.list
RUN apt-get install --no-install-recommends -y software-properties-common
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update
RUN apt-get install --no-install-recommends -y openjdk-8-jdk openjdk-7-jdk curl wget ant maven
