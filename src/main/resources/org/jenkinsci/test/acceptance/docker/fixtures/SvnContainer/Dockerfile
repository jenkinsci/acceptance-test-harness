# Sets up
FROM debian:bookworm

# Viewvc is not part of bullseye repos anymore but oldstable https://github.com/viewvc/viewvc/issues/310
RUN echo 'deb http://security.debian.org/debian-security buster/updates main' > /etc/apt/sources.list.d/oldstable.list

RUN apt-get update && apt-get install -yq apache2 libapache2-mod-svn subversion viewvc && apt-get clean

# Create a repo
# Debain defaults subversion repos to /var/lib/svn
RUN mkdir -p /var/lib/svn/
RUN svnadmin create /var/lib/svn/myrepo
ADD ./config/svnserve.conf /var/lib/svn/myrepo/conf/svnserve.conf
ADD ./config/passwd /var/lib/svn/myrepo/conf/passwd

# configure the permissions on it
RUN addgroup subversion && \
     usermod -a -G subversion www-data && \
     chown -R www-data:subversion /var/lib/svn && \
     chmod -R g+rws /var/lib/svn



# Configure /viewvc and subversion in apache
RUN /usr/sbin/a2enmod cgi
ADD ./config/dav_svn.conf /etc/apache2/mods-available/dav_svn.conf
# password files. Pre-created with "svnUser"/"test" username and password.
ADD ./config/passwd.htpasswd /etc/subversion/passwd.htpasswd
ADD ./config/viewvc.conf /etc/viewvc/viewvc.conf


# Apache needs its directories creating which the apachectl script will do (as well as validating our config!)
RUN apachectl configtest

# SSHd needs a directory for privaledge separation
RUN apt-get update && apt-get install -yq ssh && apt-get clean
RUN mkdir -p /var/run/sshd

# Create a user for testing svn+SSH
RUN useradd svnUser -d /home/svnUser && \
     mkdir -p /home/svnUser/.ssh && \
     chown svnUser /home/svnUser && \
     echo "svnUser:test" | chpasswd && \
     echo 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDzpxmTW9mH87DMkMSqBrSecoSHVCkKbW5IOO+4unak8M8cyn+b0iX07xkBn4hUJRfKA7ezUG8EX9ru5VinteqMOJOPknCuzmUS2Xj/WJdcq3BukBxuyiIRoUOXsCZzilR/DOyNqpjjI3iNb4los5//4aoKPCmLInFnQ3Y42VaimH1298ckEr4tRxsoipsEAANPXZ3p48gGwOf1hp56bTFImvATNwxMViPpqyKcyVaA7tXCBnEk/GEwb6MiroyHbS0VvBz9cZOpJv+8yQnyLndGdibk+hPbGp5iVAIsm28FEF+4FvlYlpBwq9OYuhOCREJvH9CxDMhbOXgwKPno9GyN kohsuke@atlas' > /home/svnUser/.ssh/authorized_keys
# RUN apt-get update && apt-get install -yq locales && apt-get clean locale-gen en_US.UTF-8

# Create some dummy data in the repo
RUN mkdir -p /svnRepo
ADD ./svnRepo /svnRepo
RUN /usr/sbin/apache2ctl start && \
    svn checkout http://127.0.0.1/svn/myrepo /svnRepo && \
    svn add /svnRepo/* && svn commit -m 'init' /svnRepo/* && \
    echo 'newRev' >> /svnRepo/testOne.txt && \
    svn commit -m 'Rev with changes' /svnRepo/* && \
# apache2ctl stop returns before the pid files and other things have been cleaned up so wait for the process to actually exit
    PID=`cat /var/run/apache2/apache2.pid` && apache2ctl stop && while test -d /proc/$PID; do echo waiting for apache2 to terminate; sleep 0.2; done
RUN rm -fr /svnRepo


# Install & Configure Supervisor to manage the processes
RUN apt-get update && apt-get install -yq supervisor && apt-get clean
ADD ./config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Start supervisor --> starts apache2, svnserve, and SSH
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]

EXPOSE 22
EXPOSE 80
EXPOSE 3690
