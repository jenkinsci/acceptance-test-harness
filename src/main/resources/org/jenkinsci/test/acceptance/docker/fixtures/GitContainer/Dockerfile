#
# Starts a container with sshd, git
# and prepares for execution of gitplugin tests.
#

FROM ubuntu

# make sure the package repository is up to date
RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list
RUN apt-get update

# install SSHD
RUN apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd

# install git
RUN apt-get install -y git

# create a git user and create .ssh dir
RUN useradd git -d /home/git && \
    mkdir -p /home/git/.ssh && \
    echo "git:git" | chpasswd

# adding public key to authorized keys
ADD unsafe.pub /home/git/
RUN cat /home/git/unsafe.pub >> /home/git/.ssh/authorized_keys

# run SSHD in the foreground with error messages to stderr
CMD /usr/sbin/sshd -D -e

# configure git
RUN git config --global user.email "git@gitplugin.com" && \
    git config --global user.name "Mister Git"

# create folder for git repository data and add files
RUN mkdir /home/git/repoData
ADD repoData /home/git/repoData

# create git repository and add all files
RUN cd /home/git/repoData && \
    git init && \
    git add -A && \
    git commit -am "Initial commit"

# create branch svn for checkout_branch() test
RUN cd /home/git/repoData && \
    git branch svn

# clone git repository so that the tests can access it
RUN git clone --bare /home/git/repoData /home/git/gitRepo.git

# give the whole /home/git back to the git user
RUN chown -R git /home/git