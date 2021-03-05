# Support bundle

If [support-core](https://github.com/jenkinsci/support-core-plugin) plugin is installed during the test execution, a support bundle will be captured and attached to the test scenario.
This behaviour is executed only in CI environment, and is disabled when running a test locally.

The behaviour can be overridden if necessary by setting the environment variable `CAPTURE_SUPPORT_BUNDLE=true` or `CAPTURE_SUPPORT_BUNDLE=false` depending on the desired behaviour.
