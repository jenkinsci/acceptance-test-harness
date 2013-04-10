Given /^a job inserting timestamps$/ do
  step "a job"

  @job.configure do
    find(:path, '/hudson-plugins-timestamper-TimestamperBuildWrapper').check
  end
end

When /^I set "([^"]+)" as (system time|elapsed time) timestamp format$/ do |format, kind|
  @jenkins = Jenkins::JenkinsConfig.get(@base_url, 'Jenkins global configuration')

  paths = {
    'system time' => '/hudson-plugins-timestamper-TimestamperConfig/systemTimeFormat',
    'elapsed time' => '/hudson-plugins-timestamper-TimestamperConfig/elapsedTimeFormat'
  }

  @jenkins.configure do
    find(:path, paths[kind]).set(format)
  end
end

When /^I select (system time|elapsed time|no) timestamps$/ do |kind|
  visit @job.last_build.console_url

  paths = {
    'system time' => 'timestamper-systemTime',
    'elapsed time' => 'timestamper-elapsedTime',
    'no' => 'timestamper-none'
  }

  choose paths[kind]
end

Then /^console timestamps matches regexp "([^"]+)"$/ do |pattern|
  timestamps_should match pattern
end

Then /^there are no timestamps in the console$/ do
  timestamps_should be_empty
end

def timestamps_should(match_condition)
  page.all(:xpath, '//pre/span[@class="timestamp"]').each { |timestamp|
    timestamp.text.should match_condition
  }
end
