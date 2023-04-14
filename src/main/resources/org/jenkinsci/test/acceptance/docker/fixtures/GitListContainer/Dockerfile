#
# Starts a container with sshd, git, gitlist
# and prepares for execution of gitplugin tests.
#

FROM ubuntu:22.04

RUN mkdir -p /var/run/sshd

ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Etc/UTC

# install SSHD, Git and zip
RUN apt-get update && apt-get install -y \
    openssh-server \
    git \
    zip \
    tar \
    curl \
    nginx-full \
    php8.1-fpm \
    && rm -rf /var/lib/apt/lists/*

RUN service --status-all | grep -i fpm

RUN NGINXCONFFILE=/etc/nginx/nginx.conf && echo "daemon off;" | cat - $NGINXCONFFILE > $NGINXCONFFILE.tmp && mv $NGINXCONFFILE.tmp $NGINXCONFFILE

COPY gitlist.tar.gz /var/www/
RUN cd /var/www; tar -zxvf gitlist.tar.gz
RUN chmod -R 777 /var/www/gitlist
RUN cd /var/www/gitlist/; mkdir cache; chmod 777 cache
WORKDIR /var/www/gitlist/
ADD config.ini /var/www/gitlist/
ADD nginx.conf /etc/

# create a git user and create .ssh dir
RUN useradd git -d /home/git && \
    mkdir -p /home/git/.ssh && \
    echo "git:git" | chpasswd
RUN mkdir -p /home/git/gitRepo
RUN cd /home/git/gitRepo; git --bare init .

# adding public key to authorized keys
ADD unsafe.pub /home/git/
RUN cat /home/git/unsafe.pub >> /home/git/.ssh/authorized_keys

# give the whole /home/git back to the git user
RUN chown -R git /home/git
# Proper permissions for ssh
RUN chown -R git:git /home/git/.ssh
RUN chmod 700 /home/git/.ssh
RUN chmod 600 /home/git/.ssh/authorized_keys

RUN echo "HostbasedAcceptedKeyTypes +ssh-rsa\nHostKeyAlgorithms +ssh-rsa\nPubkeyAcceptedKeyTypes +ssh-rsa" >> /etc/ssh/sshd_config

# run SSHD in the foreground with error messages to stderr
COPY run_servers.sh /

# give the whole /home/git back to the git user
RUN chown -R git /home/git

CMD ["sh", "/run_servers.sh"]