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
    GITLAB_SHARED_RUNNERS_REGISTRATION_TOKEN="" \
    GITLAB_SKIP_UNMIGRATED_DATA_CHECK=true \
    GITLAB_SKIP_RECONFIGURE=false

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
gitlab_rails['migrate_timeout'] = 60; \
gitlab_rails['db_statement_timeout'] = 15000; \
gitlab_rails['rake_cache_clear'] = false; \
gitlab_rails['db_load_balancing'] = { 'hosts' => [] }; \
puma['worker_processes'] = 1; \
puma['min_threads'] = 1; \
puma['max_threads'] = 2; \
puma['worker_timeout'] = 30; \
puma['per_worker_max_memory_mb'] = 512; \
sidekiq['concurrency'] = 5; \
sidekiq['max_retries'] = 1; \
sidekiq['queue_groups'] = ['*']; \
postgresql['max_connections'] = 50; \
postgresql['work_mem'] = '4MB'; \
postgresql['shared_buffers'] = '128MB'; \
postgresql['checkpoint_completion_target'] = 0.9; \
postgresql['wal_buffers'] = '8MB'; \
postgresql['fsync'] = 'off'; \
postgresql['synchronous_commit'] = 'off'; \
postgresql['full_page_writes'] = 'off'; \
postgresql['autovacuum'] = 'off'; \
postgresql['track_activities'] = 'off'; \
postgresql['track_counts'] = 'off'; \
postgresql['track_io_timing'] = 'off'; \
postgresql['log_statement'] = 'none'; \
postgresql['log_duration'] = 'off'; \
gitaly['configuration'] = { concurrency: [{ rpc: '/gitaly.SmartHTTPService/PostReceivePack', max_per_repo: 2 }, { rpc: '/gitaly.SSHService/SSHUploadPack', max_per_repo: 2 }], git: { catfile_cache_size: 5 } }; \
nginx['worker_processes'] = 1; \
nginx['worker_connections'] = 256; \
nginx['gzip_enabled'] = false; \
nginx['keepalive_timeout'] = 10; \
redis['maxmemory'] = '256mb'; \
redis['maxmemory_policy'] = 'allkeys-lru'; \
redis['save'] = [];"

# Expose the required ports
EXPOSE 80 443 22
