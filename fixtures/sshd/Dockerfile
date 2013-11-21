#
# Runs sshd and allow the 'test' user to login
#

FROM ubuntu

# install SSHD
RUN apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd

# create a test user
ADD setup.sh /tmp/setup.sh
RUN /tmp/setup.sh

# run SSHD in the foreground with error messages to stderr
CMD /usr/sbin/sshd -D -e
