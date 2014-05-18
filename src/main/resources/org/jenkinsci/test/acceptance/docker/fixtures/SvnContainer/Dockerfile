# Sets up
FROM ubuntu:precise
RUN apt-get update
# Install Subversion 1.8 and Apache
RUN apt-get install -y wget
RUN echo 'deb http://us.archive.ubuntu.com/ubuntu/ precise universe' >> /etc/apt/sources.list
RUN sh -c 'echo "deb http://opensource.wandisco.com/ubuntu precise svn18" >> /etc/apt/sources.list.d/WANdisco.list'
RUN wget -q http://opensource.wandisco.com/wandisco-debian.gpg -O- | apt-key add -
RUN apt-get update -y
RUN apt-get install -y subversion apache2 libapache2-svn


# Create a repo
RUN svnadmin create /home/svn
ADD ./svnserve.conf /home/svn/conf/svnserve.conf
ADD ./passwd /home/svn/conf/passwd

# Set permissions
RUN addgroup subversion && \
    usermod -a -G subversion www-data && \
    chown -R www-data:subversion /home/svn && \
    chmod -R g+rws /home/svn

# Configure Apache to serve up Subversion
RUN /usr/sbin/a2enmod auth_digest && \
    rm /etc/apache2/mods-available/dav_svn.conf
ADD ./dav_svn.conf /etc/apache2/mods-available/dav_svn.conf

# Create password files. Pre-created with "user"/"test" username and password.
RUN mkdir -p /etc/subversion
ADD ./passwd.htpasswd /etc/subversion/passwd.htpasswd

#Install Supervisor to manage the processes
RUN apt-get install -y supervisor
RUN mkdir -p /var/log/supervisor
#Add supervisor config containing the commands to execute
ADD ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#install ssh
RUN apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd
# create a test user
RUN useradd user -d /home/user && \
    mkdir -p /home/user/.ssh && \
    chown user /home/user && \
    echo "user:test" | chpasswd && \
    echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCxjLxdJCYXSuQjKg5bG9hTEuvKqueQMWcD/yiM6kqsnsb+swmO8UU1Uyp3bns5idskH0JVmqXKALOWUYEu3DIONW4nMiY+WG6Xo+0yXXPCR74SYV+gss4a+LqzxX4TMYUrInIME/QA05KnM9IkzC5SHYqu9YhE0DNJt5KC02MJVahoqJ5PvJyzORwEMxIE8e34DrnmdAmD92Gu6DCYfrC9EBv0Zwy0lWzMywx3OmvXG5V/jGhONULAnEG2k5HmK+/CZdrvHecUoBcodAsVMYlegnSzGtZtu0lzt4TiiPN/xP9uOwS/lfOXU8AUMyMrJSZsRAxhnaelfwKPTL/bmf53 itgkarl@web.de' > /home/user/.ssh/authorized_keys
RUN locale-gen en_US.UTF-8

ENV APACHE_RUN_USER    www-data
ENV APACHE_RUN_GROUP   www-data
ENV APACHE_PID_FILE    /var/run/apache2.pid
ENV APACHE_RUN_DIR     /var/run/apache2
ENV APACHE_LOCK_DIR    /var/lock/apache2
ENV APACHE_LOG_DIR     /var/log/apache2


#upload SVN data
RUN mkdir -p /svnRepo
ADD ./svnRepo /svnRepo
RUN /usr/sbin/apache2 && svn checkout http://127.0.0.1/svn /svnRepo && svn add /svnRepo/* && svn commit -m 'init' /svnRepo/*


#Start supervisor --> starts apache2
CMD ["/usr/bin/supervisord"]

EXPOSE 22
EXPOSE 80
EXPOSE 3690
