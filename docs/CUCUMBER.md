## Describing features

New feature definitions can take advantage of existing step definitions as well
as introduce its own. It is desirable to reuse steps that already exists and
keep feature specific steps separated at the same time.

While creating new features you can check whether all steps are declared
and unambiguous using `bundle exec rake cucumber:dryrun` as it is
considerably faster that actually running the scenarios.

### Features

All features are located in `features` directory and the filenames are suffixed
with `.feature` or `_plugin.feature` provided the feature describes functionality
of a plugin.

```
features/configure_slaves.feature
features/git_plugin.feature
```

### Step definitions

All step definitions are located in `src/main/java/org/jenkinsci/test/acceptance/steps`
