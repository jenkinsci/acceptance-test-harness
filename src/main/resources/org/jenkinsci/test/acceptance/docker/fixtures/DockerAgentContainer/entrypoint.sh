groupadd -g $DOCKER_GROUP docker
whoami
touch /home/test/workspace/test.txt
echo "Hello World" > /home/test/workspace/test.txt
usermod -aG docker test
chown test.test /home/test/workspace
cut -d: -f1 /etc/passwd
exec /usr/sbin/sshd -D -e
