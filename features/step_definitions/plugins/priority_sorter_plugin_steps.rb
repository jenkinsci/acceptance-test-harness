When /^I configure absolute sorting strategy with (\d+) priorities$/ do |num|
  global = Jenkins::Plugin::PrioritySorter::Global.new @jenkins
  $jenkins.configure do
    global.strategy = 'Absolute'
    global.priorities = num.to_i
  end
end

When /^I set priority (\d+) for job "(.*?)"$/ do |priority, job_name|
  page = Jenkins::Plugin::PrioritySorter::Page.new
  page.configure do
    group = page.add_group
    group.priority = priority
    group.pattern = job_name
  end
end

When /^I set priority (\d+) for view "(.*?)"$/ do |priority, view_name|
  page = Jenkins::Plugin::PrioritySorter::Page.new
  page.configure do
    group = page.add_group
    group.priority = priority
    group.view = view_name
  end
end
