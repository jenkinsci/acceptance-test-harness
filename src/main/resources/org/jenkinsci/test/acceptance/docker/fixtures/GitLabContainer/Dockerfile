# Use the official GitLab Community Edition image as the base
FROM gitlab/gitlab-ce:18.8.3-ce.0

COPY create_user.rb /usr/bin/
COPY gitlab.rb /etc/gitlab/

ENV GITLAB_SKIP_PG_UPGRADE=true \
    GITLAB_SKIP_TAIL_LOGS=true \
    GITLAB_POST_RECONFIGURE_SCRIPT=""

# Expose the required ports
EXPOSE 80 443 22
