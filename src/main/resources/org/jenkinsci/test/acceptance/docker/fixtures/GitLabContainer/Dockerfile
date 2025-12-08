# Use the official GitLab Community Edition image as the base
FROM gitlab/gitlab-ce:18.6.1-ce.0

COPY create_user.rb /usr/bin/

# Optimize GitLab for testing: disable monitoring to avoid /dev/shm exhaustion,
# disable unused services, tune for 4GB memory (Puma 2 workers, Sidekiq 10 threads)
#
ENV GITLAB_SKIP_PG_UPGRADE=true \
    GITLAB_SKIP_TAIL_LOGS=true \
    GITLAB_POST_RECONFIGURE_SCRIPT="" \
    GITLAB_ROOT_PASSWORD=testpassword123 \
    GITLAB_SHARED_RUNNERS_REGISTRATION_TOKEN=""

ENV GITLAB_OMNIBUS_CONFIG="prometheus_monitoring['enable'] = false; \
node_exporter['enable'] = false; \
redis_exporter['enable'] = false; \
postgres_exporter['enable'] = false; \
gitlab_exporter['enable'] = false; \
alertmanager['enable'] = false; \
registry['enable'] = false; \
gitlab_pages['enable'] = false; \
gitlab_kas['enable'] = false; \
sentinel['enable'] = false; \
mattermost['enable'] = false; \
storage_check['enable'] = false; \
gitlab_sshd['enable'] = false; \
logrotate['enable'] = false; \
gitlab_rails['gitlab_default_projects_features_builds'] = false; \
gitlab_rails['gitlab_default_projects_features_container_registry'] = false; \
gitlab_rails['gitlab_default_projects_features_issues'] = false; \
gitlab_rails['gitlab_default_projects_features_wiki'] = false; \
gitlab_rails['gitlab_default_projects_features_snippets'] = false; \
gitlab_rails['gitlab_email_enabled'] = false; \
gitlab_rails['incoming_email_enabled'] = false; \
gitlab_rails['terraform_state_enabled'] = false; \
gitlab_rails['packages_enabled'] = false; \
gitlab_rails['dependency_proxy_enabled'] = false; \
gitlab_rails['actioncable_enabled'] = false; \
gitlab_rails['omniauth_enabled'] = false; \
gitlab_rails['gravatar_enabled'] = false; \
gitlab_rails['automatic_issue_creation_enabled'] = false; \
gitlab_rails['gitlab_shell_ssh_port'] = 0; \
gitlab_rails['gitaly_timeout'] = 30; \
gitlab_rails['backup_keep_time'] = 0; \
gitlab_rails['usage_ping_enabled'] = false; \
gitlab_rails['sentry_enabled'] = false; \
gitlab_rails['db_pool'] = 10; \
gitlab_rails['cron_jobs'] = {}; \
gitlab_rails['auto_migrate'] = true; \
gitlab_rails['initial_root_password'] = 'testpassword123'; \
gitlab_rails['monitoring_whitelist'] = []; \
gitlab_rails['env'] = { 'MALLOC_ARENA_MAX' => '2' }; \
puma['worker_processes'] = 2; \
puma['worker_timeout'] = 60; \
puma['per_worker_max_memory_mb'] = 1024; \
sidekiq['concurrency'] = 10; \
sidekiq['max_retries'] = 3; \
postgresql['max_connections'] = 100; \
postgresql['work_mem'] = '8MB'; \
postgresql['shared_buffers'] = '256MB'; \
postgresql['checkpoint_completion_target'] = 0.9; \
postgresql['wal_buffers'] = '16MB'; \
postgresql['fsync'] = 'off'; \
postgresql['synchronous_commit'] = 'off'; \
postgresql['full_page_writes'] = 'off'; \
gitaly['configuration'] = { concurrency: [{ rpc: '/gitaly.SmartHTTPService/PostReceivePack', max_per_repo: 3 }, { rpc: '/gitaly.SSHService/SSHUploadPack', max_per_repo: 3 }] };"

# Expose the required ports
EXPOSE 80 443 22
