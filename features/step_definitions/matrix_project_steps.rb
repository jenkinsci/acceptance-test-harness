Given /^a matrix job$/ do
  @job = Jenkins::MatrixJob.create_matrix(@base_url, Jenkins::Job.random_name)
end

When /^I create a matrix job named "([^"]*)"$/ do |name|
  @job = Jenkins::MatrixJob.create_matrix(@base_url, name)
end

When /^I create a matrix job$/ do
  @job = Jenkins::MatrixJob.create_matrix(@base_url, Jenkins::Job.random_name)
end

Then /^the configuration "([^"]*)" should be built on "([^"]*)"$/ do |configuration, slave|
  config = Jenkins::MatrixConfiguration.new(@base_url, configuration, @job)
  expression = "(Building|Building remotely)( on " + slave +")"
  visit config.last_build.console.should match expression
end

Then /^combination "([^"]*)" (should|should not) be built$/ do |configuration, operator|
  visit (@job.job_url + "/#{configuration}/lastBuild")
  if operator == "should"
    page.should have_content("Build")
  else
    page.should_not have_content("Build")
  end
end

Then /^I shoud see console output of configurations matching "([^"]*)"$/ do |script|
  configurations = @job.matrix_configurations
  index = 0
  while index<configurations.length do
    configurations[index].last_build.console.should match /#{Regexp.escape(script)}/
    index += 1
  end
end

Then /^the configurations should run sequentially$/ do
  configurations = @job.matrix_configurations
  configuration_in_progress = nil
  number_finished_configurations = 0
  while number_finished_configurations < configurations.length do
     index = 0
     while index<configurations.length do # check which configuration is running and if another configuration are not running
        if configuration_in_progress == nil
          if configurations[index].last_build.in_progress? 
                configuration_in_progress = configurations[index]
          else
             not_the_last = (index + 1)< configurations.length 
             not_the_last.should be true # at least one configuration should run
          end
       else
          configurations[index].last_build.in_progress?.should be false #one configuration is running so another configuration should not run
       end
      index += 1
     end
     if configuration_in_progress != nil
        configuration_in_progress.wait_for_build("lastBuild") # wait untill configuration is finished
        sleep 2 #wait then another configuration will start
        number_finished_configurations += 1
        configuration_in_progress = nil
     end
  end
end
