gitlab_rails['max_request_duration_seconds'] = 180

gitlab_rails['gitlab_default_projects_features_issues'] = false

gitlab_rails['gitlab_default_projects_features_wiki'] = false

gitlab_rails['gitlab_default_projects_features_snippets'] = false

gitlab_rails['gitlab_default_projects_features_builds'] = false

gitlab_rails['gitlab_default_projects_features_container_registry'] = false

gitlab_rails['artifacts_enabled'] = false

gitlab_rails['usage_ping_enabled'] = false

puma['worker_timeout'] = 240

puma['worker_processes'] = 0

puma['per_worker_max_memory_mb'] = 1024

sidekiq['max_concurrency'] = 10

prometheus_monitoring['enable'] = false

gitlab_rails['env'] = {
   'GITLAB_RAILS_RACK_TIMEOUT' => 200
}

gitlab_rails['env'] = {
  'MALLOC_CONF' => 'dirty_decay_ms:1000,muzzy_decay_ms:1000'
}

gitaly['env'] = {
  'MALLOC_CONF' => 'dirty_decay_ms:1000,muzzy_decay_ms:1000',
  'GITALY_COMMAND_SPAWN_MAX_PARALLEL' => '2'
}
