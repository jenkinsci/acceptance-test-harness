require 'bundler'
require 'rake'
require 'rake/testtask'
require 'cucumber'
require 'cucumber/rake/task'

task :default => [:test,:features]

desc "Run Selenium tests locally"
Rake::TestTask.new("test") do |t|
  t.pattern = "test/selenium/**/*_test.rb"
end


namespace :cucumber do
  desc "Run the 'finished' scenarios (without @wip)"
  Cucumber::Rake::Task.new(:ready) do |t|
    t.cucumber_opts = "--tags ~@wip --format pretty"
  end

  desc "Run the scenarios which don't require network access"
  Cucumber::Rake::Task.new(:nonetwork) do |t|
    t.cucumber_opts = "--tags ~@wip --tags ~@realupdatecenter --format pretty"
  end

  desc "Run the scenarios tagged with @wip"
  Cucumber::Rake::Task.new(:wip) do |t|
    t.cucumber_opts = "--tags @wip --format pretty"
  end
end

desc "Defaults to running cucumber:ready"
task :cucumber => "cucumber:ready"
