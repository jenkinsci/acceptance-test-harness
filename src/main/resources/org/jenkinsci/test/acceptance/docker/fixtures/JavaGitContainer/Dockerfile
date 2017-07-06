#
# Starts a container with sshd, git
# and prepares for execution of gitplugin tests.
#

FROM jenkins/git:3e22c64d038a

RUN apt-get update -y && apt-get install --no-install-recommends -y software-properties-common
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update
RUN apt-get install --no-install-recommends -y openjdk-8-jdk openjdk-7-jdk curl wget ant maven

RUN useradd test -d /home/test && \
    mkdir -p /home/test/.ssh && \
    echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDzpxmTW9mH87DMkMSqBrSecoSHVCkKbW5IOO+4unak8M8cyn+b0iX07xkBn4hUJRfKA7ezUG8EX9ru5VinteqMOJOPknCuzmUS2Xj/WJdcq3BukBxuyiIRoUOXsCZzilR/DOyNqpjjI3iNb4los5//4aoKPCmLInFnQ3Y42VaimH1298ckEr4tRxsoipsEAANPXZ3p48gGwOf1hp56bTFImvATNwxMViPpqyKcyVaA7tXCBnEk/GEwb6MiroyHbS0VvBz9cZOpJv+8yQnyLndGdibk+hPbGp5iVAIsm28FEF+4FvlYlpBwq9OYuhOCREJvH9CxDMhbOXgwKPno9GyN kohsuke@atlas' > /home/test/.ssh/authorized_keys && \
    echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDagNSCDst/8z5oH9S5QWr+QNdx+haImY0FD3IQvKdD+eWI9zUbBgtoo/yYEbLvpTWiKsgT3Hw1F8mZ+/bd2Uv3lPyoG+TSzrHL4gSal6d1RWVjCOzSosciXVm4gRUvJjKXzaz8dOg+ii9yIrbeONNK0nlDUCAKy5YXSEl0avcPdUDyR3cStL6870SyanxAzktDw0n8xMq4F/alF3PZ002bcZJrmDeNVAwkP+uO2Tf8pN37SU+nApotZmlmZR32xYHnx+/OiQ7gOAVYmgNRMg0Kwh6Q73FcY3ZWCeNHwLnr95LoEAdj3On8Qr62VhGThuQNVCqBc6SeYjArfjijpcW9 jenkins-ci@localhost' >> /home/test/.ssh/authorized_keys && \
    chown -R test:test /home/test && \
    chmod 0600 /home/test/.ssh/authorized_keys && \
    echo "test:test" | chpasswd

RUN echo "KexAlgorithms curve25519-sha256@libssh.org,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha256,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group1-sha1" >> /etc/ssh/sshd_config

RUN echo "MACs hmac-sha2-512-etm@openssh.com,hmac-sha2-256-etm@openssh.com,hmac-ripemd160-etm@openssh.com,umac-128-etm@openssh.com,hmac-sha2-512,hmac-sha2-256,hmac-ripemd160,umac-128@openssh.com,hmac-md5,hmac-sha1,hmac-sha1-96,hmac-md5-96" >> /etc/ssh/sshd_config
