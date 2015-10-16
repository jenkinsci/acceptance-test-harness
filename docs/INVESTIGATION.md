# Investigating failed test

## Interactive investigation

As an alternative to set a breakpoint, that would stop the test suite and kept Jenkins and browser tunning for manual investigation. There is a environment variable `INTERACTIVE=true` to pause the suite whenever test fails.

## diagnostic information

ATH keeps track of test diagnostics information in /target/diagnostics/<TESTNAME> directory.
