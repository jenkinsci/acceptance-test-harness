#!/bin/sh

set -eu

status () {
  echo "---> ${@}" >&2
}

set -x
: LDAP_ROOTPASS=${LDAP_ROOTPASS}
: LDAP_DOMAIN=${LDAP_DOMAIN}
: LDAP_ORGANISATION=${LDAP_ORGANISATION}

if [ ! -e /var/lib/ldap/docker_bootstrapped ]; then
  status "configuring slapd for first run"
  rm -rf /etc/ldap/slapd.d/*
  (cd /etc/slapd-config ; tar cpf - .) | (cd /etc/ldap/slapd.d ; tar xpf - )
  chown -R openldap:openldap /etc/ldap
  enc_pw=$(slappasswd -h '{SSHA}' -s ${LDAP_ROOTPASS} | base64)
  enc_domain=$(echo -n ${LDAP_DOMAIN} | sed -e "s|^|dc=|" -e "s|\.|,dc=|g")
  dc_one=$(echo -n ${enc_domain} | sed -e "s|^dc=||" -e "s|,dc=.*$||g")
  sed -i -e "s|___sub_root_passwd_here___|${enc_pw}|g" \
   -e "s|___sub_domain_here___|${enc_domain}|g" \
   /etc/ldap/slapd.d/cn\=config/olcDatabase\=\{1\}mdb.ldif
  sed -i \
     -e "s|___sub_organization_here___|${LDAP_ORGANISATION}|g" \
     -e "s|___sub_dcone_here___|${dc_one}|g" \
     -e "s|___sub_domain_here___|${enc_domain}|g" \
     /etc/slapd-config/base.ldif
  slapadd -b ${enc_domain} -c -F /etc/ldap/slapd.d -l /etc/slapd-config/base.ldif
  chown -R openldap:openldap /var/lib/ldap
  touch /var/lib/ldap/docker_bootstrapped
else
  status "found already-configured slapd"
fi

status "starting slapd"
set -x
exec /usr/sbin/slapd -h "ldap:/// ldaps:/// ldapi:///" -u openldap -g openldap -d 0
