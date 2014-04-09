# Sets up
FROM ubuntu:precise

# Install Subversion 1.8 and Apache
RUN apt-get install -y wget
RUN echo 'deb http://us.archive.ubuntu.com/ubuntu/ precise universe' >> /etc/apt/sources.list
RUN sh -c 'echo "deb http://opensource.wandisco.com/ubuntu precise svn18" >> /etc/apt/sources.list.d/WANdisco.list'
RUN wget -q http://opensource.wandisco.com/wandisco-debian.gpg -O- | apt-key add -
RUN apt-get update -y
RUN apt-get install -y subversion apache2 libapache2-svn

# Create a repo
RUN svnadmin create /home/svn

# Set permissions
RUN addgroup subversion
RUN usermod -a -G subversion www-data
RUN chown -R www-data:subversion /home/svn
RUN chmod -R g+rws /home/svn

# Configure Apache to serve up Subversion
RUN /usr/sbin/a2enmod auth_digest
RUN rm /etc/apache2/mods-available/dav_svn.conf
ADD ./dav_svn.conf /etc/apache2/mods-available/dav_svn.conf

# Create password files. Pre-created with "user"/"test" username and password.
RUN mkdir -p /etc/subversion
ADD ./passwd.htpasswd /etc/subversion/passwd.htpasswd

#Install Supervisor to manage the processes
RUN apt-get install -y supervisor
RUN mkdir -p /var/log/supervisor
#Add supervisor config containing the commands to execute
ADD ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf

ENV APACHE_RUN_USER    www-data
ENV APACHE_RUN_GROUP   www-data
ENV APACHE_PID_FILE    /var/run/apache2.pid
ENV APACHE_RUN_DIR     /var/run/apache2
ENV APACHE_LOCK_DIR    /var/lock/apache2
ENV APACHE_LOG_DIR     /var/log/apache2


#Start supervisor --> starts apache2
CMD ["/usr/bin/supervisord"]


EXPOSE 80