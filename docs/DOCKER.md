# Running tests in docker container

Depending on the CI infrastructure setup one may need to run the ATH itself in a docker container with access to the host docker service, following a strategy similar to the one described in [this article](http://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/). When this is needed, the docker fixtures should not be accessed through mapped ports on the docker host, but directly through their container IP and port since they are "sibling" containers to the ATH. To enable this, set the environment variable SHARED_DOCKER_SERVICE=true, and then the functions ipBound(n) and port(n) will just return the container's IP and port where the fixture can be accessed.

TO-DO: Instead of depending on setting this environment variable, in the future we could try to somehow automatically detect the situation.

Interactive shell:

    $ docker build --build-arg=uid=$(id -u) --build-arg=gid=$(id -g) -t jenkins/ath src/main/resources/ath-container
    $ docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd):/home/ath-user/ath --user ath-user jenkins/ath bash -c 'cd $HOME/ath && bash'
    $ eval $(./vnc.sh)
    $ ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B

Jenkinsfile:

    ... // Checkout ATH
    sh 'docker build --build-arg=uid=$(id -u) --build-arg=gid=$(id -g) -t jenkins/ath src/main/resources/ath-container'
    docker.image('jenkins/ath').inside {
        sh 'eval $(./vnc.sh) && ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B'
    }
