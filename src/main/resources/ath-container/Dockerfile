# Dockerfile to be used to run ATH itself.
#
# see docs/DOCKER.md for usage

FROM ubuntu:noble

ENV LANG C.UTF-8
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
    tightvncserver \
    unzip \
    xfonts-base \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# Install a fixed firefox version that is known to work with the current selenium version, copied from https://support.mozilla.org/en-US/kb/install-firefox-linux
ARG FIREFOX_VERSION=137.0.2
RUN install -m 0755 -d /etc/apt/keyrings \
  && curl -fsSL https://packages.mozilla.org/apt/repo-signing-key.gpg -o /etc/apt/keyrings/packages.mozilla.org.asc \
  && printf 'deb [signed-by=/etc/apt/keyrings/packages.mozilla.org.asc] https://packages.mozilla.org/apt mozilla main\n' > /etc/apt/sources.list.d/mozilla.list \
  && printf 'Package: *\nPin: origin packages.mozilla.org\nPin-Priority: 1000\n' > /etc/apt/preferences.d/mozilla \
  && apt-get update \
  && DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get install --no-install-recommends -y \
    firefox="${FIREFOX_VERSION}*" \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# Docker installation according to https://docs.docker.com/engine/install/ubuntu/
ARG DOCKER_BUILDX_VERSION=0.22.0
ARG DOCKER_VERSION=28.1.0
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

# Selenium needs a geckodriver in order to work properly
ARG GECKODRIVER_VERSION=0.36.0
RUN arch=$(arch | sed s/aarch64/arm64/ | sed s/x86_64/amd64/) && \
  if [ "$arch" = "arm64" ] ; \
  then \
    curl -fsSLO "https://github.com/mozilla/geckodriver/releases/download/v${GECKODRIVER_VERSION}/geckodriver-v${GECKODRIVER_VERSION}-linux-aarch64.tar.gz" && \
    tar -xvzf "geckodriver-v${GECKODRIVER_VERSION}-linux-aarch64.tar.gz" -C /usr/local/bin; \
  else \
    curl -fsSLO "https://github.com/mozilla/geckodriver/releases/download/v${GECKODRIVER_VERSION}/geckodriver-v${GECKODRIVER_VERSION}-linux64.tar.gz" && \
    tar -xvzf "geckodriver-v${GECKODRIVER_VERSION}-linux64.tar.gz" -C /usr/local/bin ; \
  fi

# Maven in repo is not new enough for ATH
ARG MAVEN_VERSION=3.9.9
RUN curl -fsSLO "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    tar -xvzf "apache-maven-${MAVEN_VERSION}-bin.tar.gz" -C /opt/ && \
    mv "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven
ENV PATH "$PATH:/opt/maven/bin"

ENV SHARED_DOCKER_SERVICE true

COPY run.sh /usr/bin
COPY set-java.sh /usr/bin
COPY vnc.sh /usr/bin

# Allow injecting uid and git to match directory ownership
ARG uid=1001
ARG gid=1001

EXPOSE 5942

RUN deluser --remove-home ubuntu \
  && groupadd ath-user -g $gid \
  && useradd ath-user -l -c 'ATH User' -u $uid -g $gid -m -d /home/ath-user -s /bin/bash

# Give permission to modify the alternatives links to change the java version in use
RUN chmod u+s "$(which update-alternatives)"

# TODO seems this can be picked up from the host, which is unwanted:
ENV XAUTHORITY /home/ath-user/.Xauthority

USER ath-user
ENV USER ath-user
WORKDIR /home/ath-user

# 'n' for "Would you like to enter a view-only password (y/n)?"
RUN mkdir /home/ath-user/.vnc && (echo ath-user; echo ath-user; echo "n") | vncpasswd /home/ath-user/.vnc/passwd
# Default content includes x-window-manager, which is not installed, plus other stuff we do not need (vncconfig, x-terminal-emulator, etc.):
RUN touch /home/ath-user/.vnc/xstartup && chmod a+x /home/ath-user/.vnc/xstartup
RUN echo "exec /etc/X11/Xsession" > /home/ath-user/.Xsession && chmod +x /home/ath-user/.Xsession

# Prevent xauth to complain in a confusing way
RUN touch /home/ath-user/.Xauthority
