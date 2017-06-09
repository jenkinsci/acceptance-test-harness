FROM mattdm/fedora:f19
MAINTAINER Scott Hebert "shebert@redhat.com"

### Important Note about this file:
###
### This file is a replica of
### https://github.com/jenkinsci/foreman-node-sharing-plugin/blob/master/foreman-container/Dockerfile
### All changes to this file should be made in the above repo first and then replicated here.
###

RUN yum install -y http://yum.theforeman.org/releases/1.9/f19/x86_64/foreman-release.rpm
RUN yum install -y foreman foreman-sqlite foreman-console foreman-vmware foreman-compute foreman-proxy foreman-libvirt foreman-gce foreman-assets foreman-ovirt
RUN yum install -y rubygem-foreman_reserve

VOLUME /sys/fs/cgroup /run /tmp
ENV container=docker

RUN chkconfig foreman on
RUN foreman-rake permissions:reset password=changeme

CMD rm -rf /var/lock && mkdir -p /var/lock/subsys/foreman && /etc/init.d/foreman start && tail -F /var/log/foreman/production.log

EXPOSE 3000
