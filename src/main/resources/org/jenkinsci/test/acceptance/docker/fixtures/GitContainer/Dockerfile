#
# Starts a container with sshd, git
# and prepares for execution of gitplugin tests.
#

FROM ubuntu

# make sure the package repository is up to date
RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list
# install SSHD
RUN apt-get update && apt-get install -y openssh-server

RUN mkdir -p /var/run/sshd

# install git
RUN apt-get install -y git

# install zip
RUN apt-get install -y zip

# create a git user and create .ssh dir
RUN useradd git -d /home/git && \
    mkdir -p /home/git/.ssh && \
    echo "git:git" | chpasswd

# adding public key to authorized keys
ADD unsafe.pub /home/git/
RUN cat /home/git/unsafe.pub >> /home/git/.ssh/authorized_keys

# run SSHD in the foreground with error messages to stderr
CMD /usr/sbin/sshd -D -e

# give the whole /home/git back to the git user
RUN chown -R git /home/git