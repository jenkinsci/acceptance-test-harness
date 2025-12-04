# Dockerfile to be used to run ATH itself.
#
# see docs/DOCKER.md for usage

FROM ubuntu:noble

ENV LANG=C.UTF-8
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# hadolint ignore=DL3008
RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get install --no-install-recommends -y \
    ca-certificates \
    curl \
    git \
    imagemagick \
    jq \
    lsb-release \
    openjdk-17-jdk \
    openjdk-21-jdk \
    openssh-client \
    unzip \
    xfonts-base \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# Docker installation according to https://docs.docker.com/engine/install/ubuntu/
ARG DOCKER_BUILDX_VERSION=0.30.1
ARG DOCKER_VERSION=29.1.2
RUN install -m 0755 -d /etc/apt/keyrings \
  && curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc \
  && printf 'deb [arch=%s signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu %s stable\n' "$(dpkg --print-architecture)" "$(lsb_release -cs)" > /etc/apt/sources.list.d/docker.list \
  && apt-get update \
  && DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get install --no-install-recommends -y \
    docker-buildx-plugin="${DOCKER_BUILDX_VERSION}*" \
    docker-ce="5:${DOCKER_VERSION}*" \
    docker-ce-cli="5:${DOCKER_VERSION}*" \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# Maven in repo is not new enough for ATH
ARG MAVEN_VERSION=3.9.11
RUN curl -fsSLO "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    curl -fsSLO "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz.sha512" && \
    echo "$(cat apache-maven-${MAVEN_VERSION}-bin.tar.gz.sha512)  apache-maven-${MAVEN_VERSION}-bin.tar.gz" | sha512sum --check --strict && \
    tar -xvzf "apache-maven-${MAVEN_VERSION}-bin.tar.gz" -C /opt/ && \
    mv "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven && \
    rm "apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    rm "apache-maven-${MAVEN_VERSION}-bin.tar.gz.sha512"
ENV PATH="$PATH:/opt/maven/bin"

COPY run.sh /usr/bin
COPY set-java.sh /usr/bin

# Allow injecting uid and git to match directory ownership
ARG uid=1001
ARG gid=1001

RUN deluser --remove-home ubuntu \
  && groupadd ath-user -g $gid \
  && useradd ath-user -l -c 'ATH User' -u $uid -g $gid -m -d /home/ath-user -s /bin/bash

# Give permission to modify the alternatives links to change the java version in use
RUN chmod u+s "$(which update-alternatives)"

USER ath-user
ENV USER=ath-user
WORKDIR /home/ath-user
