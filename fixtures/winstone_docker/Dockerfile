#
# Container for running jenkins.war
#
# See winstone_docker_controller.rb
#

FROM jenkins/sshd

# JDK is from Universe
RUN echo deb http://archive.ubuntu.com/ubuntu precise universe >> /etc/apt/sources.list
RUN apt-get update
RUN apt-get install --no-install-recommends -y openjdk-7-jdk curl wget ant maven
