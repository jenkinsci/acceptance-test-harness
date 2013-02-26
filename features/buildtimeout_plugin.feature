Feature: Fail builds that take too long
  In order to prevent executors from being blocked for too long
  As a Jenkins user
  I want to set timeouts with the build-timeout plugin to abort or
  fail builds that exceed specified timeout values

  # NOTE: The build-timeout plugin doesn't allow timeouts less than 3 minutes
  # in duration, so I'm just going to leave this commented out :(
  #@wip @realupdatecenter
  #Scenario: Fail a blocked job
  #  Given I have installed the "build-timeout" plugin
  #  And a job
  #  When I configure the job
  #  And I add a script build step to run "sleep 10000"
  #  And I set the build timeout to 3 minutes
  #  When I build the job
  #  Then the build should fail
