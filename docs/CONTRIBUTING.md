# Contribution guidlines
 
## Recognized use-cases

This test harness is maintained with following use-cases in mind. In case you are interested in other usage, there is no guarantee it will work as expected or that the particular use-case will not be broken by future updates. Please stick with recognized use-cases or propose your use-case to be declared here to be on the safe side.

- Jenkins core PR verification testsuite.
- Library for UI testing used by Jenkins components.
- Periodic verification of the health of the upstream Jenkins distribution.
  - Latest releases of components are being tested together to spot problems early.
- Verification of release candidates and their compatibility with plugins.
- Verification of custom combination of components (Jenkins core and plugin versions).
  - Pre-deployment tests.
  - Internal update center verification.

## Test contribution

In order for tests to be added it should:

- have significant user-base (prominent core feature, feature of a plugin installed by at least 1% of deployments)
- represent realistic, nontrivial and fundamental use-case
- covering sufficiently distinct use-case than other ATH tests
- plugin tests should have testsuite executed in less than 20 minutes on public CI server

Existing tests that does not adhere to these guidelines will be reevaluated and proposed for restructure or move to the component test suite by test/component maintainers. In case of not enough interest, the test will be dropped.

## Dealing with fragile/broken tests

Test is determined fragile/broken when it is failing despite no defects is present in tested component.

- JIRA issue will be created notifying author of the test or plugin authors.
  - Test can be fixed or moved away to the sources of the plugin.
- Test would be `@Ignore`d while the JIRA is open
- At the end of the time period, the test would be removed from ATH.
  - Test can still be resurrected from history if needed - in ATH or plugin itself.

The situation is expected to be handled in 90 days since reported. In case of no corrective action, the test will be removed from ATH.
