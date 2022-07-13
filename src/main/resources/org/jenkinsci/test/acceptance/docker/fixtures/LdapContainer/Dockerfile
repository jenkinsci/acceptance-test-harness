#
# Dockerfile for openLDAP (slapd)
# Manager-DN (for bind): cn=admin,dc=jenkins-ci,dc=org
# Manager-Password: jenkins
# See config/base.ldif for user/groups directory entries
#
# VERSION=2
#
FROM phusion/baseimage:focal-1.2.0

ENV HOME /root

# Disable SSH
RUN rm -rf /etc/service/sshd /etc/my_init.d/00_regen_ssh_host_keys.sh

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]

# Install slapd
RUN LC_ALL=C DEBIAN_FRONTEND=noninteractive apt-get -y update && apt-get install -y slapd && apt-get clean

# Default configuration: can be overridden at the docker command line
ENV LDAP_ROOTPASS jenkins
ENV LDAP_ORGANISATION Jenkins CI
ENV LDAP_DOMAIN jenkins-ci.org

# Expose ports
EXPOSE 389 636

RUN mkdir /etc/service/slapd /etc/slapd-config

ADD config /etc/slapd-config
RUN cp /etc/slapd-config/slapd.sh /etc/service/slapd/run && chmod 755 /etc/service/slapd/run && chown root:root /etc/service/slapd/run
