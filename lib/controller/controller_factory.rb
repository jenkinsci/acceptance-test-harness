require 'future'

class JenkinsControllerFactory
  # instantiate a new JenkinsController for the next test
  # @param opts [Hash]  bag of controller specific options
  # @return [JenkinsController]
  def create(opts)
    # to be implemented by subtypes
    raise NotImplementedException
  end
end

#
# Factory of JenkinsController that picks up the one based on environment variables
#
class DefaultJenkinsControllerFactory < JenkinsControllerFactory
  def initialize()
    # default is to run locally, but allow the parameters to be given as env vars
    # so that rake can be invoked like "rake test type=remote_sysv"
    if ENV['type']
      @controller_args = {}
      ENV.each { |k,v| @controller_args[k.to_sym]=v }
    else
      @controller_args = { :type => :local }
    end
  end

  def create(opts)
    JenkinsController.create(@controller_args.merge(opts))
  end
end



# JenkinsControllerFactory that pre-launches instances to speed up
class CachedJenkinsControllerFactory < JenkinsControllerFactory

  # @param nested [JenkinsControllerFactory]  nested factory that actually creates JenkinsController
  #
  def initialize(nested)
    @nested = nested

    @cache = {}

    at_exit do
      shutdown
    end
  end

  def create(opts)
    opts = opts.clone

    x = @cache[opts]
    if x.nil?   # no cached instance available, need to launch
      x = @nested.create(opts)
    else
      puts "Waiting for pre-launched instance to complete initialization"
      x = x.__force__    # retrieve the promised value
    end

    # pre-launch the next instance asynchronously
    @cache[opts] = future do
      y = @nested.create(opts)
      y.prestart
      y
    end

    x
  end

  # clean up unused instances that were prelaunched
  def shutdown
    @cache.each_value do |c|
      c.stop
      c.teardown
    end
  end
end