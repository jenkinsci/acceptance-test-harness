#
# Runs sshd and allow the 'test' user to login
#

FROM ubuntu

# install FTP
RUN apt-get update
RUN apt-get install -y vsftpd
RUN echo "local_enable=YES" >> /etc/myftp.conf
RUN echo "write_enable=YES" >> /etc/myftp.conf
RUN echo "listen=YES" >> /etc/myftp.conf
RUN echo "anonymous_enable=YES" >> /etc/myftp.conf
RUN echo "dirmessage_enable=YES" >> /etc/myftp.conf
RUN echo "use_localtime=YES" >> /etc/myftp.conf
RUN echo "xferlog_enable=YES" >> /etc/myftp.conf
RUN echo "secure_chroot_dir=/var/run/vsftpd/empty" >> /etc/myftp.conf
RUN echo "pam_service_name=vsftpd" >> /etc/myftp.conf
RUN echo "rsa_cert_file=/etc/ssl/private/vsftpd.pem" >> /etc/myftp.conf
RUN echo "pasv_enable=YES" >> /etc/myftp.conf
RUN echo "pasv_min_port=7050" >> /etc/myftp.conf
RUN echo "pasv_max_port=7055" >> /etc/myftp.conf
RUN mkdir -p /var/run/vsftpd/empty

# create a test user
RUN useradd test -d /home/test && \
        mkdir -p /home/test/ftp && \
        chown test /home/test && \
        echo "test:test" | chpasswd

# run VSFTPD
CMD /usr/sbin/vsftpd /etc/myftp.conf

