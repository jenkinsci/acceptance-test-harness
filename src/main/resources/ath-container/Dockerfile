# Dockerfile to be used to run ATH itself.
#
# see docs/DOCKER.md for usage

FROM ubuntu:22.04

# hadolint ignore=DL3008
RUN apt-get update && \
  apt-get install -y --no-install-recommends \
    gpg-agent \
    software-properties-common \
    && \
  apt-get clean all && rm -rf /var/cache/apt

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# hadolint ignore=DL3008
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    TZ=Etc/UTC \
    apt-get install -y --no-install-recommends \
        # docker dep
        ca-certificates \
        gnupg \
        lsb-release \
        curl \
        git \
        imagemagick \
        iptables \
        unzip \
        tightvncserver \
        openjdk-11-jdk \
        xfonts-base \
        openssh-client \
        && \
    apt-get clean all && rm -rf /var/cache/apt

# Install a fixed firefox version that is known to work with the current selenium version, copied from https://hub.docker.com/r/selenium/node-firefox/dockerfile
ARG FIREFOX_VERSION=108.0.2
# hadolint: Do not pin packages as they are requirements only for firefox installation (and not ofr the rest of the ATH) and the effort required to track each version would be too huge
# hadolint ignore=DL3008
RUN apt-get -y --no-install-recommends install libgtk-3-0 libasound2 libdbus-glib-1-2 libx11-xcb-dev libpci-dev libgl1-mesa-glx wget bzip2 \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
  && curl --silent https://download-installer.cdn.mozilla.net/pub/firefox/releases/${FIREFOX_VERSION}/linux-x86_64/en-US/firefox-${FIREFOX_VERSION}.tar.bz2 --output /tmp/firefox.tar.bz2 \
  && rm -rf /opt/firefox \
  && tar -C /opt -xjf /tmp/firefox.tar.bz2 \
  && rm /tmp/firefox.tar.bz2 \
  && mv /opt/firefox /opt/firefox-${FIREFOX_VERSION} \
  && ln -fs /opt/firefox-${FIREFOX_VERSION}/firefox /usr/bin/firefox

# Selenium needs a geckodriver in order to work properly
ENV GECKODRIVER_VERSION 0.32.2
# gross due to https://github.com/mozilla/geckodriver/issues/1956
RUN arch=$(arch | sed s/aarch64/arm64/ | sed s/x86_64/amd64/) && \
  if [ "$arch" = "arm64" ] ; \
  then \
    curl -fsSLO https://github.com/jamesmortensen/geckodriver-arm-binaries/releases/download/v${GECKODRIVER_VERSION}/geckodriver-v${GECKODRIVER_VERSION}-linux-aarch64.tar.gz && \
    tar -xvzf geckodriver-v${GECKODRIVER_VERSION}-linux-aarch64.tar.gz -C /usr/local/bin; \
  else \
    curl -fsSLO https://github.com/mozilla/geckodriver/releases/download/v${GECKODRIVER_VERSION}/geckodriver-v${GECKODRIVER_VERSION}-linux64.tar.gz && \
    tar -xvzf geckodriver-v${GECKODRIVER_VERSION}-linux64.tar.gz -C /usr/local/bin ; \
  fi

# Maven in repo is not new enough for ATH
ENV MAVEN_VERSION 3.9.1
RUN curl -ffSLO https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-"$MAVEN_VERSION"-bin.tar.gz && \
    tar -xvzf apache-maven-"$MAVEN_VERSION"-bin.tar.gz -C /opt/ && \
    mv /opt/apache-maven-* /opt/maven
ENV PATH="$PATH:/opt/maven/bin"

# Docker installation according to https://docs.docker.com/engine/install/ubuntu/
RUN mkdir -p /etc/apt/keyrings && chmod 0755 /etc/apt/keyrings /etc/apt/ /etc
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

RUN echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list

# Ensure that version is fixed
ARG DOCKER_VERSION=23.0.1
ARG DOCKER_BUILDX_VERSION=0.10.2
RUN apt-get update --quiet \
  && apt-get install --yes --no-install-recommends \
    docker-ce="5:${DOCKER_VERSION}*" \
    docker-ce-cli="5:${DOCKER_VERSION}*" \
    docker-buildx-plugin="${DOCKER_BUILDX_VERSION}*" \
  && apt-get clean \
  && rm -rf /var/cache/apt

ENV SHARED_DOCKER_SERVICE true

# Allow injecting uid and git to match directory ownership
ARG uid=1001
ENV uid $uid
ARG gid=1001
ENV gid $gid

EXPOSE 5942

RUN groupadd ath-user -g $gid && \
    useradd ath-user -l -u $uid -g $gid -m -d /home/ath-user && \
    # Give permission to modify the alternatives links to change the java version in use
    chown ath-user:ath-user /etc/alternatives

# TODO seems this can be picked up from the host, which is unwanted:
ENV XAUTHORITY /home/ath-user/.Xauthority

USER ath-user
# 'n' for "Would you like to enter a view-only password (y/n)?"
RUN mkdir /home/ath-user/.vnc && (echo ath-user; echo ath-user; echo "n") | vncpasswd /home/ath-user/.vnc/passwd
# Default content includes x-window-manager, which is not installed, plus other stuff we do not need (vncconfig, x-terminal-emulator, etc.):
RUN touch /home/ath-user/.vnc/xstartup && chmod a+x /home/ath-user/.vnc/xstartup
RUN echo "exec /etc/X11/Xsession" > /home/ath-user/.Xsession && chmod +x /home/ath-user/.Xsession

# Prevent xauth to complain in a confusing way
RUN touch /home/ath-user/.Xauthority

# Set SUID and SGID for docker binary so it can communicate with mapped socket its uid:gid we can not control. Alternative
# approach used for this is adding ath-user to the group of /var/run/docker.sock but that require root permission we do not
# have in ENTRYPOINT as the container is started as ath-user.
USER root
RUN chmod ug+s /usr/bin/docker*

COPY vnc.sh /usr/bin/
COPY set-java.sh /usr/bin/
COPY run.sh /usr/bin/

RUN chmod u+s "$(which update-alternatives)"
RUN dbus-uuidgen > /etc/machine-id

USER ath-user

ENV USER ath-user
