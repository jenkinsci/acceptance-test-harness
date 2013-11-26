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

        @runner = JenkinsControllerFactory.get.create(@controller_options||{})
        @runner.start
        $version = @runner.jenkins_version

        @base_url = @runner.url
        Capybara.app_host = @base_url

        @jenkins = Jenkins::JenkinsRoot.new(@base_url)

        # TODO: scenario.skip_not_applicable($version)

        begin
          test.run
        ensure
          # @runner.diagnose if scenario.failed?
          @runner.stop # if test fails, stop in at_exit is not called
          @runner.teardown
        end
      end

      self.module_eval(&block)
    end
  end
end
