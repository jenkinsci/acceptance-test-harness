#
# Runs smbd and allow the 'test' user to connect
#

FROM ubuntu

RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list
RUN apt-get update

RUN useradd test -d /home/test -s /bin/bash && \
        mkdir -p /home/test && \
        chown test /home/test && \
        echo "test:test" | chpasswd

RUN apt-get install -y samba

RUN echo "workgroup = WORKGROUP" >> /etc/mysmb.conf
RUN echo "restrict anonymous = no" >> /etc/mysmb.conf
RUN echo "server string = DockerJenkinsSMB" >> /etc/mysmb.conf
RUN echo "netbios name = DockerJenkinsSMB" >> /etc/mysmb.conf
RUN echo "security = share" >> /etc/mysmb.conf

RUN echo "[tmp]" >> /etc/mysmb.conf
RUN echo "  comment = Temp" >> /etc/mysmb.conf
RUN echo "  path = /tmp" >> /etc/mysmb.conf
RUN echo "  guest ok = Yes" >> /etc/mysmb.conf
RUN echo "  read only = no" >> /etc/mysmb.conf

CMD ["/usr/sbin/smbd", "-i", "-s", "/etc/mysmb.conf"]
