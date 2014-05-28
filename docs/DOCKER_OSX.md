# Working with Docker on Mac OS X

Many developers work on OS X platforms due to similarity of Unix based tooling on Linux and Mac OS X. This approach
has its limits when dealing with tests executing a third party process in a Docker container.

## What Docker supports

As of Docker version 0.11 which is stated to be a release candidate for 1.0 (first stable Docker release), Docker does
not natively run on Mac OS X. This is due of Dockers implementation to rely on Linux kernel virtualization features.
Obviously, Mac OS X isn't a Linux clone nor does it provide the *exactly the same* virtualization features as Linux
kernel does. To help that out there is a project called `boot2docker`.

The idea of `boot2docker` is to start a very thin Linux layer as a VirtualBox VM. All Docker containers a going to run
within the `boot2docker` VM. But this is done transparently to the Mac user, so that the user is still able to use
`docker` commands in the Mac OS X terminals and does not need to make an SSH connection with the `boot2docker` VM.

                                                +-----------------------------------------------------------------------+
                                                |                           boot2docker container                       |
                                                |                                                                       |
                                                |                                                                       |
     -----docker command for container 1------> |                                         +-----------------------+     |
                                                |----- command for container 1 ---------->|    container 1        |     |
                                                |                                         |                       |     |
                                                |<---- result after command execution ----|                       |     |
     <---- result after command execution ----- |                                         |                       |     |
                                                |                                         +-----------------------+     |
                                                |                                                                       |
     -----docker command for container 2------> |                                         +-----------------------+     |
                                                |----- command for container 2 ---------->|    container 2        |     |
                                                |                                         |                       |     |
                                                |<---- result after command execution ----|                       |     |
     <---- result after command execution ----- |                                         |                       |     |
                                                |                                         +-----------------------+     |
                                                |                                                  ...                  |
                                                |                                                                       |
                                                |                                         +-----------------------+     |
                                                |                                         |    container N        |     |
                                                |                                         |                       |     |
                                                |                                         |                       |     |
                                                |                                         |                       |     |
                                                |                                         +-----------------------+     |
                                                |                                                                       |
                                            +---------------------------------------------------------------------------+


## Setup of `boot2docker`

## Prerequisites

Mac OS X with pre-installed VirtualBox.


## Installation

**Please Note!** All commands are assumed to be run as a non-root (without `sudo`) user. If there is a need to run a
command with sudo, this is going to be explicitly stated.

The easiest way to setup `boot2docker` is to run `brew install boot2docker` command. This command also installs the
`docker` command on Mac OS X.

Now you can execute `boot2docker` command in the shell of your choice.

Now you need to init and start `boot2docker` VM, so that VirtualBox knows about it.

    boot2docker init
    boot2docker up


### Update `boot2docker`

I suggest to update your `boot2docker` VM by executing the commands:

    boot2docker stop
    boot2docker download


### Create VM rules to allow direct access to ports exported by `docker`

**Please note!** VM must be switched off for the subsequent command to succeed.

The next step is going to allow you direct connection with your `docker` containers on exported steps. The trick here
is to map all possible ports to the Host machine that all docker containers might be using. This can be done
automatically with a VirtualBox management tool:

    for i in {49000..49900}; do
     VBoxManage modifyvm "boot2docker-vm" --natpf1 "tcp-port$i,tcp,,$i,,$i";
     VBoxManage modifyvm "boot2docker-vm" --natpf1 "udp-port$i,udp,,$i,,$i";
    done

This aproach is described in ["Port Forwarding on Steroids"](https://github.com/boot2docker/boot2docker/blob/master/doc/WORKAROUNDS.md)
section. There is also an explanation on how to remove forwarded ports.

Finally, you need to start the `boot2docker` container again:

    boot2docker up

### Working with `docker` command from the shell

To work with docker containers from the shell one needs to export an environment variable where the `docker` command
should be sending the commands to:

    export DOCKER_HOST=tcp://localhost:4243

Port `4243` is going to be forwarded in the `boot2docker` VM config automatically.

### Status Quo

Described setup works well with tests which only need to start/stop docker container and finally use standard network
protocols: ssh, http, ftp etc.

Problems start when test cases start to deviate from the standard protocols and rely on _extended_ docker functionality.

This functionality might not be properly working. From my experience all docker file commands (e.g. `EXPOSE`, `ADD`, `RUN`)
were working properly. But some of the docker controlling commands failed (e.g. `cp`). This experience is summarized in
the next section.

### Caveats

#### Copying data from the `docker` container after the test case execution

Some test cases which were testing proper functioning of SSH executions in Jenkins did not want to rely at the end of
the test case on SSH protocol to copy produced data back for comparison. These test cases used `docker cp` to copy back
the files. On Mac OS X `docker cp` via `boot2docker` fails with 'Permission denied' error. There are numerous bugs filed
for that and also numerous workarounds. These workarounds are rely on an introduction of the file sharing into
`boot2docker`. Unfortunately, that is not easily done as `boot2docker` is a really minimalist Linux distribution. These
solutions require manual rebuilding of `boot2docker` VM with VirtualBox guest tool additions. For someone who just wants
to start writing/executing tests this is too much effort and might be not very well reproducible.

### IP address binding

Some test cases assumed that `docker` VM should be listening on a particular local ip address like `127.0.0.5` and not
on `0.0.0.0`. This makes the test fail, as the port forwarding requires more complex forwarding rules. All test cases
must start docker container listening on IP `0.0.0.0`.


## Conclusion

Working from Mac OS X with `boot2docker` can be done with test cases, which rely on default Unix functionality
or protocols, e.g. start a process via SSH and copy its output via SSH. Or use http server started in the Docker
container.

If you need additional Docker controlling command like `cp` you might fail.

Overall, docker feels a bit raw on Mac OS X right now, but there seems to be a great effort to make it work.
