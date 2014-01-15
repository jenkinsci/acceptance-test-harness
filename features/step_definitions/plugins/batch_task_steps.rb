When /^I add batch task "(.*?)"$/ do |name|
  task = Plugin::BatchTask::Declaration.add(@job)
  task.name = name
end

When /^I configure batch trigger for "(.*?)"$/ do |task|
  @job.save
  # Needed to save configured batch tasks before configuring triggers
  @job.configure
  step %{I configure "#{@job.name}" batch trigger for "#{task}"}
end

When /^I configure "(.*?)" batch trigger for "(.*?)"$/ do |job, task|
  @job.add_postbuild_step('Invoke batch tasks').task(job, task)
end

When /^I run "(.*?)" batch task manually$/ do |task|
  task(@job, task).build!
end

Then /^the batch task "(.*?)" (should|should not) run$/ do |task, should_or_not|
  task(@job, task).send should_or_not, exist
end

Then /^"(.*?)" batch task "(.*?)" (should|should not) run$/ do |job_name, task, should_or_not|
  task($jenkins.job(job_name), task).send should_or_not, exist
end

def task(job, task)
  Plugin::BatchTask::Task.new(job, task)
end
