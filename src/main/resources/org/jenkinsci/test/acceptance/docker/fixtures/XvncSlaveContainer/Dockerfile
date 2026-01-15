# originally SshdContainer/Dockerfile

#
# Runs sshd and allow the 'test' user to login
#

FROM ubuntu:noble

# install SSHD
RUN apt-get update -y && \
  apt-get install -y \
  openssh-server \
  locales
RUN mkdir -p /var/run/sshd

# create a test user
RUN useradd test -d /home/test && \
  mkdir -p /home/test/.ssh && \
  echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDzpxmTW9mH87DMkMSqBrSecoSHVCkKbW5IOO+4unak8M8cyn+b0iX07xkBn4hUJRfKA7ezUG8EX9ru5VinteqMOJOPknCuzmUS2Xj/WJdcq3BukBxuyiIRoUOXsCZzilR/DOyNqpjjI3iNb4los5//4aoKPCmLInFnQ3Y42VaimH1298ckEr4tRxsoipsEAANPXZ3p48gGwOf1hp56bTFImvATNwxMViPpqyKcyVaA7tXCBnEk/GEwb6MiroyHbS0VvBz9cZOpJv+8yQnyLndGdibk+hPbGp5iVAIsm28FEF+4FvlYlpBwq9OYuhOCREJvH9CxDMhbOXgwKPno9GyN kohsuke@atlas' > /home/test/.ssh/authorized_keys && \
  echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDagNSCDst/8z5oH9S5QWr+QNdx+haImY0FD3IQvKdD+eWI9zUbBgtoo/yYEbLvpTWiKsgT3Hw1F8mZ+/bd2Uv3lPyoG+TSzrHL4gSal6d1RWVjCOzSosciXVm4gRUvJjKXzaz8dOg+ii9yIrbeONNK0nlDUCAKy5YXSEl0avcPdUDyR3cStL6870SyanxAzktDw0n8xMq4F/alF3PZ002bcZJrmDeNVAwkP+uO2Tf8pN37SU+nApotZmlmZR32xYHnx+/OiQ7gOAVYmgNRMg0Kwh6Q73FcY3ZWCeNHwLnr95LoEAdj3On8Qr62VhGThuQNVCqBc6SeYjArfjijpcW9 jenkins-ci@localhost' >> /home/test/.ssh/authorized_keys && \
  chown -R test:test /home/test && \
  chmod 0600 /home/test/.ssh/authorized_keys && \
  echo "test:test" | chpasswd

# https://stackoverflow.com/a/38553499/12916
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
  dpkg-reconfigure --frontend=noninteractive locales && \
  update-locale LANG=en_US.UTF-8
ENV LANG=en_US.UTF-8

# run SSHD in the foreground with error messages to stderr
ENTRYPOINT ["/usr/sbin/sshd", "-D", "-e"]

# originally JavaContainer/Dockerfile as of https://github.com/jenkinsci/docker-fixtures/pull/138

RUN apt-get update && \
  apt-get install --no-install-recommends -y \
  software-properties-common \
  openjdk-21-jdk-headless \
  curl \
  ant \
  maven

# additional setup for XvncSlaveContainer

RUN apt-get update && apt-get install -y tigervnc-standalone-server imagemagick

# So it is owned by root and has the permissions vncserver seems to require:
RUN mkdir /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix/

# TODO seems this can be picked up from the host, which is unwanted:
ENV XAUTHORITY=/home/test/.Xauthority

USER test
RUN mkdir /home/test/.vnc && (echo jenkins; echo jenkins) | vncpasswd /home/test/.vnc/passwd
# Default content includes x-window-manager, which is not installed, plus other stuff we do not need (vncconfig, x-terminal-emulator, etc.):
RUN touch /home/test/.vnc/xstartup && chmod a+x /home/test/.vnc/xstartup
USER root
