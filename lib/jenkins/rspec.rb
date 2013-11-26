#
# Defines the main body of spec/spec_helper.rb that glues the test harness with rspec
#
require 'jenkins/env'
require 'jenkins/jenkins'
require 'pry'

module Jenkins
  # Use this method like you use 'describe'. This adds
  # a hook around the test to launch Jenkins and put [JenkinsRoot] in @jenkins
  def self.rspec(name,&block)
    describe name do
      around do |test|
        # skip scenario and initialization when not applicable
        # TODO: pending if scenario.skip_not_applicable($version)

        with_jenkins(@controller_options||{}) do |j|
          @jenkins = j
          @runner = j.controller

          $version = @runner.jenkins_version

          @base_url = @runner.url
          Capybara.app_host = @base_url

          # TODO: scenario.skip_not_applicable($version)

          test.run
        end
      end

      self.module_eval(&block)
    end
  end
end


module RSpec
  module Core
    # Extend RSpec DSL
    class ExampleGroup
      # Run the given block with JenkinsRoot and tear it down in the end
      # use this inside the 'it do ... end' block
      def with_jenkins(opts,&block)
        begin
          j = JenkinsControllerFactory.get.create(opts)
          j.start
          yield Jenkins::JenkinsRoot.new(j)
        ensure
          j.stop
          j.teardown
        end
      end
    end
  end
end
