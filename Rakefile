require 'bundler'
require 'rake'
require 'rake/testtask'

task :default => [:test]

desc "Run Selenium tests locally"
Rake::TestTask.new("test") do |t|
  t.pattern = "test/selenium/**/*_test.rb"
end
