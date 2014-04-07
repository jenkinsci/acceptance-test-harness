#!/bin/bash

##
## Touches $HOME/terminate.txt file if there is at least 1 sshd process
## is found running by the user owning this process. It would wait for $2
##
## $1 username
## $2 timout to wait before shutdown
##
## author: Vivek Pandey
##
function usage(){
  echo "USAGE: sh autoterminate.sh USERNAME TIMEOUT"
  exit 1
}

if [ -z "$1" ]
  then
    usage
fi

if [ -z "$2" ]
  then
    usage
fi

if [ $# -eq 0 ]
  then
    usage
fi

echo "Executing with username=$1, timeout=$2"

FILE=$HOME/terminate.txt
echo touching file $FILE

touch $FILE

while(true)
do
  num_of_sshd=$(pgrep -u $1 sshd |wc -l)
  if [ -z "$num_of_sshd" ]
    then
      num_of_sshd=0
  fi

  echo Number of sshd processes: $num_of_sshd

  if [ $num_of_sshd -gt 0 ]
  then
    echo "Touching file..."
    touch $FILE
  fi

  if test `find "$FILE" -cmin +$2`
  then
    echo "Shutting down now..."
    sudo shutdown -h now
    exit 0
  else
    echo Not yet, waiting for $2 minutes
  fi
  echo "Going to sleep for 10 min"
  sleep 600
done



