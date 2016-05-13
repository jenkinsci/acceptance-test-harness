FROM mattdm/fedora:f19
MAINTAINER Scott Hebert "shebert@redhat.com"

RUN yum install -y http://yum.theforeman.org/releases/1.5/f19/x86_64/foreman-release.rpm
RUN yum install -y foreman foreman-sqlite foreman-console foreman-vmware foreman-compute foreman-proxy foreman-libvirt foreman-gce foreman-assets foreman-ovirt
RUN yum install -y rubygem-foreman_reserve

VOLUME /sys/fs/cgroup /run /tmp
ENV container=docker

RUN chkconfig foreman on
CMD mkdir -p /var/lock/subsys/foreman && /etc/init.d/foreman start && tail -F /var/log/foreman/production.log

EXPOSE 3000
