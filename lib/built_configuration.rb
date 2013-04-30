module Jenkins
  class BuiltConfiguration < PageObject
    attr_reader :build, :configuration

    def initialize(build, configuration)
      @build = build
      @configuration = configuration
      super(build.build_url, nil)
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
