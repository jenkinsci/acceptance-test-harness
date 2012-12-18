#!/bin/bash -e

LIB_DIR="./lib" # if changed, JENKINS_LIB_DIR variable in test/selenium/lib/base.rb needs to be changed as well!
JENKINS_WAR="$LIB_DIR/jenkins.war"

function prepare_lib_dir {
    # LIB_DIR doesn't exist
    if [ ! -d $LIB_DIR ]; then
	echo "Creating lib dir ($LIB_DIR)"
	mkdir $LIB_DIR
    fi
}

function clean_lib_dir {
    # LIB_DIR is not empty
    if [ !`ls -A $LIB_DIR` ]; then
    	echo "Lib dir ($LIB_DIR) is not empty, cleaning"
	rm -rf $LIB_DIR
    fi
    
}

function grab_latest_rc {
    curl -L "http://mirrors.jenkins-ci.org/war-rc/latest/jenkins.war" > $JENKINS_WAR
}

function grab_latest_lts {
    curl -L "http://mirrors.jenkins-ci.org/war-stable/latest/jenkins.war" > $JENKINS_WAR
}

function extract_slave {
    prepare_lib_dir
    tmp_dir=`mktemp -d`
    unzip $JENKINS_WAR -d $tmp_dir
    if [ -r $tmp_dir/WEB-INF/slave.jar ]; then
	cp $tmp_dir/WEB-INF/slave.jar $LIB_DIR
    else
	echo "slave.jar ($tmp_dir/WEB-INF/slave.jar) wasn't found, exit!"
	rm -rf $tmp_dir
	exit 1
    fi
    rm -rf $tmp_dir
}


extract_slave



