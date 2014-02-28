module Jenkins
  # Determine whether an execution of scenario should be skipped
  class ScenarioSkipper

    @@skippers = []

    def self.register
      raise "#{self.class.name} already registered" if @@skippers.include? self

      @@skippers << self.new
    end

    def self.should_run?(scenario)
      for skipper in @@skippers
        if !skipper.should_run scenario
          return false
        end
      end

      return true
    end

    def self.should_run_against?(scenario, runner)
      for skipper in @@skippers
        if !skipper.should_run_against scenario, runner
          return false
        end
      end

      return true
    end

    # Determine whether an execution of scenario should be skipped
    #
    # @param scenario [Cucumber::Ast::Scenario] a scenario to examine
    def should_run(scenario)
      true
    end

    # Called after Jenkins has started. Use `should_run` if running instance is not needed
    #
    # @param scenario [Cucumber::Ast::Scenario] a scenario to examine
    # @param runner   [JenkinsController]       Jenkins instance (can be nil)
    def should_run_against(scenario, runner)
      true
    end
  end

  class SinceSkipper < ScenarioSkipper
    register

    def should_run_against(scenario, runner)
      tag = scenario.tag('since')
      return true if tag.nil?

      required_version = Gem::Version.new(tag.values[0])

      return runner.jenkins_version >= required_version
    end
  end

  class NativeCommandSkipper < ScenarioSkipper
    register

    def should_run(scenario)
      tag = scenario.tag('native')
      return true if tag.nil?

      commands = tag.values.join(' ')
      system('which ' + commands, :out => '/dev/null')
    end
  end
end
