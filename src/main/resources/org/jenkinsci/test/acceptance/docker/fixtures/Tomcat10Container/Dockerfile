#
# Runs Tomcat10 on Ubuntu at port 8080, with the admin app
#
# The admin user has username 'admin' and password 'tomcat'
#

FROM ubuntu:noble

RUN apt-get update && apt-get install -y tomcat10 tomcat10-admin

# configure the admin user
RUN echo '<tomcat-users><role rolename="tomcat"/><role rolename="manager-gui"/><role rolename="admin-gui"/><role rolename="manager-script"/><user username="admin" password="tomcat" roles="tomcat,admin-gui,manager-gui,manager-script"/></tomcat-users>' > /etc/tomcat10/tomcat-users.xml

EXPOSE 8080
CMD CATALINA_BASE=/var/lib/tomcat10/ /usr/share/tomcat10/bin/catalina.sh run
