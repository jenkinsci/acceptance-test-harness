#!/bin/bash

DOMAIN="localdomain"
DOMAIN_ID=1

ENVIRONMENT="test_env2"
ENVIRONMENT_ID=1

ARCH="x86_64"
ARCHID=1

OPERATINGSYSTEM="Fedora"
OPERATINGSYSTEM_ID=1
MAJOR="23"
MINOR=""

PARTITION="Kickstart default"
PTABLE_ID=7
MEDIUM="Fedora Mirror"
MEDIA_ID=3

HOSTGROUP="test-group"
HOSTGROUP_ID=1

HOST_ID=1

MACADDRESS="50:7b:9d:4d:f1:39"
JENKINS_SLAVE_REMOTEFS_ROOT="/tmp/remoteFSRoot"

USER="admin" 
PASS="changeme" 

if [ -z "$1" ] ; then
  FOREMAN_URL="http://localhost:32768/api/v2"
else
  FOREMAN_URL="$1"
fi

if [ -z "$2" ] ; then
  HOSTNAME="localhost"
else
  HOSTNAME="$2"
fi

if [ -z "$3" ] ; then
  HOSTIP="127.0.0.1"
else
  HOSTIP="$3"
fi

if [ -z "$4" ] ; then
  EXAMPLE_LABEL="label1 aix"
else
  EXAMPLE_LABEL="$4"
fi

domainCreateString="{ \"domain\": { \"name\": \"$DOMAIN\" } }"

envCreateString="{ \"environment\": { \"name\": \"$ENVIRONMENT\" } }"

osCreateString="{ \"operatingsystem\": { \"name\": \"$OPERATINGSYSTEM\", \"major\": \"$MAJOR\", \"minor\": \"$MINOR\", \"architecture_ids\":  $ARCHID, \"ptable_ids\": $PTABLE_ID, \"medium_ids\": $MEDIA_ID    } }"

hostGroupCreateString="{ \"hostgroup\": {  \"name\": \"$HOSTGROUP\", \"environment_id\": $ENVIRONMENT_ID, \"domain_id\": $DOMAIN_ID, \"architecture_id\":  $ARCHID, \"operatingsystem_id\": $OPERATINGSYSTEM_ID, \"medium_id\": $MEDIA_ID, \"ptable_id\": $PTABLE_ID, \"root_pass\": \"scottscott\" } }"

hostCreateString="{ \"host\": { \"name\": \"$HOSTNAME\", \"domain_id\": $DOMAIN_ID, \"hostgroup_id\": $HOSTGROUP_ID, \"root_pass\": \"xybxa6JUkz63w\", \"mac\": \"$MACADDRESS\" , \"architecture_id\":  $ARCHID, \"operatingsystem_id\": $OPERATINGSYSTEM_ID, \"medium_id\": $MEDIA_ID, \"ptable_id\": $PTABLE_ID, \"environment_id\": $ENVIRONMENT_ID } }"

    #{ \"name\": \"JENKINS_LABEL\", \"value\": \"$EXAMPLE_LABEL\" }, \
    #{ \"name\": \"JENKINS_SLAVE_REMOTEFS_ROOT\", \"value\": \"/tmp/remoteFSRoot\" }, \
hostUpdateString="{ \"host\": { \"name\": \"$HOSTNAME\", \"ip\": \"$HOSTIP\", \"host_parameters_attributes\": [ { \"name\": \"RESERVED\", \"value\": \"false\" } , { \"name\": \"JENKINS_LABEL\", \"value\": \"$EXAMPLE_LABEL\" }, { \"name\": \"JENKINS_SLAVE_REMOTEFS_ROOT\", \"value\": \"/tmp/remoteFSRoot\" }] } }"

echo ""
echo "** Creating domain $DOMAIN"
curl -g -H "Content-Type: application/json" \
    -X POST -d "$domainCreateString" \
    -k -u $USER:$PASS \
    $FOREMAN_URL/domains

echo ""
echo "** Creating environment $ENVIRONMENT"
curl -g -H "Content-Type: application/json" \
    -X POST -d "$envCreateString" \
    -k -u $USER:$PASS \
    $FOREMAN_URL/environments

echo ""
echo "** Creating operating system $OPERATINGSYSTEM $MAJOR"
curl -g -H "Content-Type: application/json" \
    -X POST -d "$osCreateString" \
    -k -u $USER:$PASS \
    $FOREMAN_URL/operatingsystems

echo ""
echo "** Creating host group $HOSTGROUP"
curl -g -H "Content-Type: application/json" \
    -X POST -d "$hostGroupCreateString" \
    -k -u $USER:$PASS \
    $FOREMAN_URL/hostgroups

echo ""
echo "** Creating host $HOSTNAME"
curl -g -H "Content-Type: application/json" \
    -X POST -d "$hostCreateString" \
    -k -u $USER:$PASS \
    $FOREMAN_URL/hosts

echo ""
echo "** Updating host $HOSTNAME"
curl -g -H "Content-Type: application/json" \
    -X PUT -d "$hostUpdateString" \
    -k -u $USER:$PASS \
    $FOREMAN_URL/hosts/$HOST_ID

echo ""
echo "** Done"
echo ""
