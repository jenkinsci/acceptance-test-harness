FROM centos:7
MAINTAINER ogondza@gmail.com

ARG HOST_NAME
ENV HOST_NAME $HOST_NAME

RUN yum -y install\
    krb5-libs\
    krb5-server\
    krb5-workstation\
    base64coder

ADD src/prepare.sh /tmp/prepare_kerberos.sh
ADD src/etc.krb5.conf /etc/krb5.conf
ADD src/var.kerberos.krb5kdc.kadm5.acl /var/kerberos/krb5kdc/kadm5.acl
ADD src/keytab.sh keytab.sh



# Run the service
CMD sh /tmp/prepare_kerberos.sh


