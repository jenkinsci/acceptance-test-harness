#!/bin/bash

# Create a keytab on path=$1 for principal=$2.

# This is run from container so the host do not have to install kerberos stuff.

set -eo pipefail

rm -rf "$1" && mkdir -p "$(dirname "$1")"
ktutil <<EOF
add_entry -password -p $2 -k 1 -e des-cbc-md5
ATH
add_entry -password -p $2 -k 1 -e des-cbc-crc
ATH
add_entry -password -p $2 -k 1 -e rc4-hmac
ATH
write_kt $1
EOF

# This is run as root but we are doing this to export keytabs to the hosts. Reset
# the owner to the same user as the surrounding directory.
chown --reference=$(dirname $1) $1
