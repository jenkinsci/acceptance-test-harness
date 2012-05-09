require 'bundler'
require 'rake'
require 'rake/testtask'
require 'cucumber'
require 'cucumber/rake/task'

task :default => [:test,:features]

desc "Run Selenium tests locally"
Rake::TestTask.new("test") do |t|
  t.pattern = "test/selenium/noop_test.rb"
  # t.pattern = "test/selenium/**/*_test.rb"
end

Cucumber::Rake::Task.new(:features) do |t|
  t.cucumber_opts = "features --format pretty"
end