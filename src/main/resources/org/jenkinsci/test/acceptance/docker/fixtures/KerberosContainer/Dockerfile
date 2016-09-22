FROM centos:7
MAINTAINER ogondza@gmail.com

ARG HOST_NAME
ENV HOST_NAME $HOST_NAME

RUN yum -y install\
    krb5-libs\
    krb5-server\
    krb5-workstation

ADD src/etc.krb5.conf /etc/krb5.conf
ADD src/var.kerberos.krb5kdc.kadm5.acl /var/kerberos/krb5kdc/kadm5.acl
ADD src/keytab.sh keytab.sh

# Populate kdc database with principals
RUN rm -f /dev/random && ln -s /dev/urandom /dev/random &&\
    /usr/sbin/kdb5_util create -s -P "ATH" &&\
    /usr/sbin/kadmin.local -w ATH -q "addprinc -pw ATH -clearpolicy -e des-cbc-md5:normal,des-cbc-crc:normal,rc4-hmac:normal user" &&\
    /usr/sbin/kadmin.local -w ATH -q "addprinc -pw ATH -clearpolicy -e des-cbc-md5:normal,des-cbc-crc:normal,rc4-hmac:normal HTTP/${HOST_NAME}"

# Generate keytabs
RUN mkdir -p /target/keytab &&\
    bash keytab.sh /target/keytab/service HTTP/${HOST_NAME}@EXAMPLE.COM &&\
    bash keytab.sh /target/keytab/user user@EXAMPLE.COM

# Run the service
CMD /usr/sbin/_kadmind -P /var/run/kadmind.pid &&\
    /usr/sbin/krb5kdc -P /var/run/krb5kdc.pid &&\
    tail -f /var/log/krb5kdc.log
