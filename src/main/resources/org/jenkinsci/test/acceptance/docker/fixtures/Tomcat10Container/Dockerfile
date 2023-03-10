#
# Runs Tomcat7 on Ubuntu at port 8080, with the admin app
#
# The admin user has username 'admin' and password 'tomcat'
#

FROM ubuntu:22.04

RUN apt-get update && apt-get install -y gnupg

# Tomcat7 is from Universe
RUN echo "deb http://archive.ubuntu.com/ubuntu lunar universe" >> /etc/apt/sources.list
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 40976EAF437D05B5
RUN apt-get update && apt-get install -y tomcat10 tomcat10-admin

# configure the admin user
RUN echo '<tomcat-users><role rolename="tomcat"/><role rolename="manager-gui"/><role rolename="admin-gui"/><role rolename="manager-script"/><user username="admin" password="tomcat" roles="tomcat,admin-gui,manager-gui,manager-script"/></tomcat-users>' > /etc/tomcat10/tomcat-users.xml

EXPOSE 8080
CMD CATALINA_BASE=/var/lib/tomcat10/ /usr/share/tomcat10/bin/catalina.sh run
