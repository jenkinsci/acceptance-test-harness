ADDRESS=`hostname -I`
ADDRESS=`echo -e "${ADDRESS}" | tr -d '[:space:]'`
rm -f /dev/random && ln -s /dev/urandom /dev/random
/usr/sbin/kdb5_util create -s -P "ATH"
/usr/sbin/kadmin.local -w ATH -q "addprinc -pw ATH -clearpolicy -e des-cbc-md5:normal,des-cbc-crc:normal,rc4-hmac:normal user"
/usr/sbin/kadmin.local -w ATH -q "addprinc -pw ATH -clearpolicy -e des-cbc-md5:normal,des-cbc-crc:normal,rc4-hmac:normal HTTP/localhost"


sed -i "s@__KDC_PORT__@88@g; s@__ADMIN_PORT__@749@g; s@_ADDRESS_@${ADDRESS}@g" /etc/krb5.conf
mkdir -p /target/keytab

bash keytab.sh /target/keytab/service HTTP/localhost@EXAMPLE.COM
bash keytab.sh /target/keytab/user user@EXAMPLE.COM

/usr/sbin/_kadmind -P /var/run/kadmind.pid && /usr/sbin/krb5kdc -P /var/run/krb5kdc.pid
touch /tmp/kerberos_running
tail -f /var/log/krb5kdc.log

