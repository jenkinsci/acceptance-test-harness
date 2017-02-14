groupadd -g $DOCKER_GROUP docker
usermod -aG docker test
chown test.test /home/test/workspace
exec /usr/sbin/sshd -D -e
