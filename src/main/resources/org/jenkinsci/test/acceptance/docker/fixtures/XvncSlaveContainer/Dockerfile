FROM evarga/jenkins-slave
RUN apt-get install -y vnc4server imagemagick

# So it is owned by root and has the permissions vncserver seems to require:
RUN mkdir /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix/

# TODO seems this can be picked up from the host, which is unwanted:
ENV XAUTHORITY /home/jenkins/.Xauthority

USER jenkins
RUN mkdir /home/jenkins/.vnc && (echo jenkins; echo jenkins) | vncpasswd /home/jenkins/.vnc/passwd
# Default content includes x-window-manager, which is not installed, plus other stuff we do not need (vncconfig, x-terminal-emulator, etc.):
RUN touch /home/jenkins/.vnc/xstartup && chmod a+x /home/jenkins/.vnc/xstartup
USER root
