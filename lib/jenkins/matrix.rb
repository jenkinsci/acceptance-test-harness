require 'jenkins/job'

#                 MatrixJob
#                  /     \
# MatrixConfiguration   MatrixBuild
#                  \     /
#          MatrixConfigurationBuild
module Jenkins
  class MatrixJob < Job

    register 'Matrix', 'hudson.matrix.MatrixProject'

    def initialize(*args)
      super(*args)
    end

    # override
    def build(number)
      Jenkins::MatrixBuild.new(self, number)
    end

    def add_user_axis(name,value)
       ensure_config_page
       find(:xpath, "//button[text()='Add axis']").click
       find(:xpath, "//li/a[text()='User-defined Axis']").click
       sleep 0.1 # wait until axis appear
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.TextAxis']//td/input";
       find(:xpath, "(#{input}[@name='_.name'])[last()]").set(name)
       find(:xpath, "(#{input}[@name='_.valueString'])[last()]").set(value)
    end

    def add_label_expression_axis(value)
       ensure_config_page
       find(:xpath, "//button[text()='Add axis']").click
       find(:xpath, "//li/a[text()='Label expression']").click
       sleep 0.1 # wait until axis appear
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.LabelExpAxis']//td/input";
       find(:xpath, "(#{input}[@name='_.name'])[last()]").set(value)
    end

    def add_slaves_axis(value)
       ensure_config_page
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.LabelAxis']//td/input";
       if !(page.has_xpath?("//div[@name='axis' and @descriptorid='hudson.matrix.LabelAxis']"))
          find(:xpath, "//button[text()='Add axis']").click
          find(:xpath, "//li/a[text()='Slaves']").click
          sleep 0.1 # wait until axis appear
       end

       checkbox = find(:xpath, "#{input}[@name='values' and @json='#{value}']", visible: false)

       if !checkbox.visible?
          find(:xpath, "//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel1']//a").click
          find(:xpath, "//div[@class='yahooTree labelAxis-tree']//table[@id='ygtvtableel2']//a").click
       end

       checkbox.check
    end

    def add_jdk_axis(value)
       ensure_config_page
       find(:xpath, "//button[text()='Add axis']").click
       if !(page.has_xpath?(:xpath, "//li/a[text()='JDK']"))
          find(:xpath, "//button[text()='Add axis']").click
          find(:xpath, "//li/a[text()='JDK']").click
          sleep 0.1 # wait until axis appear
       end
       input = "//div[@name='axis' and @descriptorid='hudson.matrix.JDKAxis']//td/input";
       find(:xpath, "(#{input}[@name='values' and @json='#{value}'])[last()]").set(true)
    end

    def combination_filter(filter)
       ensure_config_page
       find(:xpath, "//input[@name='hasCombinationFilter']").set(true)
       find(:xpath, "//input[@name='combinationFilter']").set(filter)
    end

    def run_configurations_sequentially
       ensure_config_page
       find(:xpath, "//input[@name='_.runSequentially']").set(true)
    end

    def touchstone_builds_first(filter, result)
       ensure_config_page
       find(:xpath, "//input[@name='_.hasTouchStoneCombinationFilter']").set(true)
       find(:xpath, "//input[@name='_.touchStoneCombinationFilter']").set(filter)
       find(:xpath, "//select[@name='touchStoneResultCondition']/option[@value='#{result}']").click
    end

    def configuration(name)
      Jenkins::MatrixConfiguration.new(self, name)
    end

    def configurations
      configurations = []
      json['activeConfigurations'].each do |config|

        configurations << configuration(config['name'])
      end

      return configurations
    end
  end

  class MatrixConfiguration < PageObject
    attr_accessor :timeout, :job, :combination

    def initialize(job, combination)
      super(@base_url, 'Matrix configuration')
      @timeout = 60 # Default all builds for this job to a 60s timeout
      @job = job
      @combination = combination
    end

    def url
      "#{@job.url}/#{@combination}"
    end

    def json_api_url
      "#{url}/api/json"
    end

    def open
      visit(url)
    end

    def last_build
      return build("lastBuild") # Hacks!
    end

    def workspace
      Jenkins::Workspace.new(url)
    end

    def build(number)
      Jenkins::MatrixConfigurationBuild.new(MatrixBuild.new(@job, number), self)
    end
  end

  class MatrixBuild < Build

    def initialize(job, number)
      super(job.base_url, job, number)
    end

    def configurations
      configuration_builds = []
      @job.configurations.each do |config|
        configuration_builds << MatrixConfigurationBuild.new(self, config)
      end

      return configuration_builds
    end

    def configuration(name)
      Jenkins::MatrixConfigurationBuild.new(self, Jenkins::MatrixConfiguration.new(job, name))
    end
  end

  class MatrixConfigurationBuild < Build
    attr_reader :build, :configuration

    # do not invoke superclass constructor
    def initialize(build, configuration)
      @build = build
      @configuration = configuration
      @base_url = build.base_url
      @name = 'Matrix configuration build'
    end

    def url
      "#{@build.url}/#{@configuration.combination}"
    end

    def json_api_url
      "#{url}/api/json"
    end

    # Determine whether the combination was built in a given build
    def exists?
      number = @build.json['number']

      @build.json['runs'].each do |run|
        if run['number'] == number
          return true if run['url'].include?(@configuration.combination)
        end
      end

      return false
    end
  end
end
