#
# Runs sshd and allow the 'test' user to login
#

FROM ubuntu

# install SSHD
RUN apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd

# create a test user
RUN useradd test -d /home/test && \
    mkdir -p /home/test/.ssh && \
    chown test /home/test && \
    echo "test:test" | chpasswd && \
    echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDzpxmTW9mH87DMkMSqBrSecoSHVCkKbW5IOO+4unak8M8cyn+b0iX07xkBn4hUJRfKA7ezUG8EX9ru5VinteqMOJOPknCuzmUS2Xj/WJdcq3BukBxuyiIRoUOXsCZzilR/DOyNqpjjI3iNb4los5//4aoKPCmLInFnQ3Y42VaimH1298ckEr4tRxsoipsEAANPXZ3p48gGwOf1hp56bTFImvATNwxMViPpqyKcyVaA7tXCBnEk/GEwb6MiroyHbS0VvBz9cZOpJv+8yQnyLndGdibk+hPbGp5iVAIsm28FEF+4FvlYlpBwq9OYuhOCREJvH9CxDMhbOXgwKPno9GyN kohsuke@atlas' > /home/test/.ssh/authorized_keys

# run SSHD in the foreground with error messages to stderr
CMD /usr/sbin/sshd -D -e
