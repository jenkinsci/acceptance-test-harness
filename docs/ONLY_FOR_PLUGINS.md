# Running only tests related to certain plugins

Sometime you may want to run tests related to certain plugins. For that you can set the `ONLY_FOR_PLUGINS` to
the comma-separated list of plugins id's you are interested in.

When this variable is set plugins which does not have a `WithPlugins` annotation that includes
one of the provided plugins or plugins that depend on those will be skipped.

Please be aware that plugins not using the `JenkinsAcceptanceRule` are not affected and will be
executed unless excluded in some other way.

