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

# No Snaps please https://www.omgubuntu.co.uk/2022/04/how-to-install-firefox-deb-apt-ubuntu-22-04
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN add-apt-repository ppa:mozillateam/ppa && \
  printf 'Package: firefox*\nPin: version 102.8.0*\nPin-Priority: 1001' | tee /etc/apt/preferences.d/mozilla-firefox

# hadolint ignore=DL3008
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    TZ=Etc/UTC \
    apt-get install -y --no-install-recommends \
        curl \
        docker \
        git \
        imagemagick \
        iptables \
        firefox-esr \
        unzip \
        tightvncserver \
        openjdk-11-jdk \
        xfonts-base \
        && \
    apt-get clean all && rm -rf /var/cache/apt

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
ENV MAVEN_VERSION 3.9.0
RUN curl -ffSLO https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-"$MAVEN_VERSION"-bin.tar.gz && \
    tar -xvzf apache-maven-"$MAVEN_VERSION"-bin.tar.gz -C /opt/ && \
    mv /opt/apache-maven-* /opt/maven
ENV PATH="$PATH:/opt/maven/bin"

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

COPY vnc.sh /usr/bin/
COPY set-java.sh /usr/bin/
COPY run.sh /usr/bin/

RUN chmod u+s "$(which update-alternatives)"
RUN dbus-uuidgen > /etc/machine-id
RUN ln -s /lib/firefox-esr/firefox.sh /usr/bin/firefox

USER ath-user

ENV USER ath-user
