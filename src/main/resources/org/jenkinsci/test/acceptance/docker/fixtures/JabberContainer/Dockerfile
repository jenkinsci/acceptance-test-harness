#
# Setup Prosody IM XMPP Server with two users
#

FROM ubuntu:latest

# Needed for supervisord
RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list
# install prosody
RUN LC_ALL=C DEBIAN_FRONTEND=noninteractive apt-get update && apt-get install -y prosody
RUN apt-get upgrade -y

# configure prosody
RUN sed -i -e 's/admins = { }/admins = { "admin@localhost" }/g' /etc/prosody/prosody.cfg.lua
RUN sed -i -e 's/VirtualHost "example.com"/VirtualHost "localhost"/g' /etc/prosody/prosody.cfg.lua
RUN sed -i -e 's/enabled = false -- Remove this line to enable this host/enabled = true/g' /etc/prosody/prosody.cfg.lua
RUN sed -i -e 's/daemonize = true/daemonize = false/g' /etc/prosody/prosody.cfg.lua
RUN sed -i -e 's/--Component "conference.example.com" "muc"/Component "conference.localhost" "muc"/g' /etc/prosody/prosody.cfg.lua
RUN sed -i -e 's|-- Syslog:|debug = "/var/log/prosody/prosody.debug";|g' /etc/prosody/prosody.cfg.lua

# Symlink SSL Key files
RUN ln -s /etc/prosody/certs/localhost.key /etc/prosody/certs/example.com.key
RUN ln -s /etc/prosody/certs/localhost.cert /etc/prosody/certs/example.com.crt

# create needed folders
RUN mkdir -p /var/run/prosody
RUN chown prosody:prosody /var/run/prosody
RUN touch /var/log/prosody/prosody.debug
RUN chown prosody:prosody /var/log/prosody/prosody.debug

# setup test user
RUN prosodyctl register admin localhost admin-pw
# setup jenkins-ci user
RUN prosodyctl register jenkins-ci localhost jenkins-pw
# setup bot user
RUN prosodyctl register bot localhost bot-pw

# Install python
#RUN LC_ALL=C DEBIAN_FRONTEND=noninteractive apt-get install -y python python-pip



# Install Supervisor to manage the processes
RUN LC_ALL=C DEBIAN_FRONTEND=noninteractive apt-get install -y supervisor
RUN mkdir -p /var/log/supervisor
# Add supervisor config containing the commands to execute
ADD ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Install python xmpp logbot, python is already there by supervisor package
RUN LC_ALL=C DEBIAN_FRONTEND=noninteractive apt-get install -y python-pip
RUN pip install logbot

# XMPP Default Port
#EXPOSE 5222 5000
EXPOSE 5222
# Logfile /.logbot/logs/test/20140613.txt

ENV __FLUSH_LOG 1
# start supervisord
CMD ["/usr/bin/supervisord"]