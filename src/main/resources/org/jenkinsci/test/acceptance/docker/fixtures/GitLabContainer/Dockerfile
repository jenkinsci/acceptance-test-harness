# Use the official GitLab Community Edition image as the base
FROM gitlab/gitlab-ce:18.6.1-ce.0

COPY create_user.rb /usr/bin/

# Optimize GitLab for testing environments
#
# CRITICAL: Disabling Prometheus monitoring is required to avoid /dev/shm exhaustion.
# Without this, GitLab requires customizing minimum --shm-size=512m (default id 64MB)
#
# Service Disables:
# - Monitoring: prometheus, node_exporter, redis_exporter, postgres_exporter, alertmanager
# - Optional services: registry, pages, kas
# - CI features: default builds, container registry (tests only use repositories)
# - Repository maintenance: housekeeping (ephemeral repos don't need Git optimization)
# - Email: gitlab_email_enabled (tests don't send notifications)
#
# Resource Tuning (optimized for memory-constrained environments),
# see: https://docs.gitlab.com/omnibus/settings/memory_constrained_envs
# - PostgreSQL: shared_buffers=128MB (safe minimum for testing)
# - Puma: 0 workers (non-clustered mode)
# - Sidekiq: 10 concurrency
# - Gitaly: Limited RPC concurrency (max 3 per repo) and parallel spawns (max 2)
# - jemalloc: Aggressive memory decay (1s) for faster memory reclamation
#
ENV MALLOC_CONF="dirty_decay_ms:1000,muzzy_decay_ms:1000"
ENV GITALY_COMMAND_SPAWN_MAX_PARALLEL=2
ENV GITLAB_OMNIBUS_CONFIG="prometheus_monitoring['enable'] = false; \
node_exporter['enable'] = false; \
redis_exporter['enable'] = false; \
postgres_exporter['enable'] = false; \
alertmanager['enable'] = false; \
registry['enable'] = false; \
gitlab_pages['enable'] = false; \
gitlab_kas['enable'] = false; \
gitlab_rails['gitlab_default_projects_features_builds'] = false; \
gitlab_rails['gitlab_default_projects_features_container_registry'] = false; \
gitlab_rails['gitlab_default_projects_features_housekeeping'] = false; \
gitlab_rails['gitlab_email_enabled'] = false; \
postgresql['shared_buffers'] = '128MB'; \
puma['worker_processes'] = 0; \
sidekiq['max_concurrency'] = 10; \
gitaly['configuration'] = { concurrency: [{ rpc: '/gitaly.SmartHTTPService/PostReceivePack', max_per_repo: 3 }, { rpc: '/gitaly.SSHService/SSHUploadPack', max_per_repo: 3 }] }"

# Expose the required ports
EXPOSE 80 443 22
