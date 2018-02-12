FROM jenkins/java:6894df180381
RUN apt-get update && apt-get install -y vnc4server imagemagick

# So it is owned by root and has the permissions vncserver seems to require:
RUN mkdir /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix/

# TODO seems this can be picked up from the host, which is unwanted:
ENV XAUTHORITY /home/test/.Xauthority

USER test
RUN mkdir /home/test/.vnc && (echo jenkins; echo jenkins) | vncpasswd /home/test/.vnc/passwd
# Default content includes x-window-manager, which is not installed, plus other stuff we do not need (vncconfig, x-terminal-emulator, etc.):
RUN touch /home/test/.vnc/xstartup && chmod a+x /home/test/.vnc/xstartup
USER root
