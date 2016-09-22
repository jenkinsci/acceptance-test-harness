#!/bin/bash

set -eo pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

host_name="$(hostname -f)"
service_keytab="$DIR/target/keytab/service"
kdc_cid_file="$DIR/target/kdc.cid"

rm -rf "$DIR/target"; mkdir -p "$DIR/target/keytab"
trap "rm -rf '$DIR/target'" EXIT

docker build -t kerberos-kdc --build-arg=HOST_NAME=$host_name .
docker run -p 88 -p 749 --cidfile="$kdc_cid_file" kerberos-kdc &
sleep 1
cid="$(cat $kdc_cid_file)"
trap "echo -e '\nAborting KDC...'; docker kill $cid; docker rm $cid" EXIT

docker cp "$cid:/target/keytab" "$DIR/target/"

kdc_port=$(docker port $cid 88 | sed "s/0.0.0.0://")
admin_port=$(docker port $cid 749 | sed "s/0.0.0.0://")

sed "s@__HOST_NAME__@${host_name}@g; s@__SERVICE_KEYTAB__@${service_keytab}@g" "$DIR/src/login.conf" > "$DIR/target/login.conf"
sed "s@__KDC_PORT__@${kdc_port}@g; s@__ADMIN_PORT__@${admin_port}@g" "$DIR/src/etc.krb5.conf" > "$DIR/target/etc.krb5.conf"

echo -e "\n!!!\n"
echo "login.conf $DIR/target/login.conf"
echo "krb5.conf $DIR/target/etc.krb5.conf"
echo "service.keytab $service_keytab"
echo "user.keytab $DIR/target/keytab/user"
echo -e "\n!!!\n"

tail -f /dev/stdin
