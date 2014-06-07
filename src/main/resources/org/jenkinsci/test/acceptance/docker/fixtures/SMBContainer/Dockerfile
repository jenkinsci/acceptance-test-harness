#
# Runs sshd and allow the 'test' user to login
#

FROM ubuntu:latest

# Configure apt
RUN echo 'deb http://us.archive.ubuntu.com/ubuntu/ precise universe' >> /etc/apt/sources.list
RUN apt-get -y update
#install samba
RUN apt-get install -y samba


# create a test user
RUN useradd test -d /home/test && \
        mkdir -p /home/test/ftp && \
        chown test /home/test && \
        echo "test:test" | chpasswd

RUN echo "workgroup = WORKGROUP" >> /etc/mysmb.conf
RUN echo "restrict anonymous = no" >> /etc/mysmb.conf
RUN echo "server string = DockerJenkinsSMB" >> /etc/mysmb.conf
RUN echo "netbios name = DockerJenkinsSMB" >> /etc/mysmb.conf
RUN echo "security = share" >> /etc/mysmb.conf

RUN echo "[tmp]" >> /etc/mysmb.conf
RUN echo "  comment = Temp" >> /etc/mysmb.conf
RUN echo "  path = /tmp" >> /etc/mysmb.conf
RUN echo "  guest ok = Yes" >> /etc/mysmb.conf
RUN echo "  read only=no" >> /etc/mysmb.conf

CMD /usr/sbin/smbd -i -s /etc/mysmb.conf