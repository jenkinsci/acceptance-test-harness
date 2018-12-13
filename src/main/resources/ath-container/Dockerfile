# Dockerfile to be used to run ATH itself.
#
# see docs/DOCKER.md for usage


FROM debian:buster
MAINTAINER ogondza@gmail.com

RUN apt-get clean && \
    apt-get -y update && \
    apt-get install -y \
        curl \
        git \
        imagemagick \
        iptables \
        firefox-esr \
        maven \
        unzip \
        vnc4server \
        openjdk-8-jdk \
        openjdk-11-jdk

# All we need is a statically linked client library - no need to install daemon deps: https://get.docker.com/builds/
RUN curl -fsSLO https://download.docker.com/linux/static/stable/x86_64/docker-17.03.2-ce.tgz && \
    tar --strip-components=1 -xvzf docker-17.03.2-ce.tgz -C /usr/local/bin

# Selenium needs a geckodriver in order to work properly
RUN curl -fsSLO https://github.com/mozilla/geckodriver/releases/download/v0.22.0/geckodriver-v0.22.0-linux64.tar.gz && \
    tar -xvzf geckodriver-v0.22.0-linux64.tar.gz -C /usr/local/bin

ENV SHARED_DOCKER_SERVICE true

# Allow injecting uid and git to match directory ownership
ARG uid=1000
ENV uid $uid
ARG gid=1000
ENV gid $gid

EXPOSE 5942

# So it is owned by root and has the permissions vncserver seems to require:
RUN mkdir /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix/

RUN groupadd ath-user -g $gid && \
    useradd ath-user -u $uid -g $gid -m -d /home/ath-user && \
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
RUN chmod ug+s "$(which docker)"

# Java 8 by default
RUN update-java-alternatives -s java-1.8.0-openjdk-amd64

# Set at the end to allow docker cache
ARG JAXB_API_VERSION="2.3.0"
ARG JAXB_VERSION="2.3.0.1"
ARG JAF_VERSION="1.2.0"

# Checksum for JavaBean Activation Framework lib is not matching the Maven Central one.
RUN mkdir -p /home/ath-user/jdk11-libs && \
    curl -LSs https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/${JAXB_API_VERSION}/jaxb-api-${JAXB_API_VERSION}.jar -o /home/ath-user/jdk11-libs/jaxb-api.jar && \
    curl -LSs https://repo1.maven.org/maven2/com/sun/xml/bind/jaxb-core/${JAXB_VERSION}/jaxb-core-${JAXB_VERSION}.jar -o /home/ath-user/jdk11-libs/jaxb-core.jar && \
    curl -LSs https://repo1.maven.org/maven2/com/sun/xml/bind/jaxb-impl/${JAXB_VERSION}/jaxb-impl-${JAXB_VERSION}.jar -o /home/ath-user/jdk11-libs/jaxb-impl.jar && \
    curl -LSs https://repo1.maven.org/maven2/com/sun/activation/${JAF_VERSION}/javax.activation-${JAF_VERSION}.jar -o /home/ath-user/jdk11-libs/javax.activation.jar && \
    echo "99f802e0cb3e953ba3d6e698795c4aeb98d37c48  /home/ath-user/jdk11-libs/jaxb-api.jar\n\
    23574ca124d0a694721ce3ef13cd720095f18fdd  /home/ath-user/jdk11-libs/jaxb-core.jar\n\
    2e979dabb3e5e74a0686115075956391a14dece8  /home/ath-user/jdk11-libs/jaxb-impl.jar\n\
    84e709cb8271e5e7ff7da61528d52d36298aa733  /home/ath-user/jdk11-libs/javax.activation.jar" | sha1sum -c
