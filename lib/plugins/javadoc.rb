require File.dirname(__FILE__) + "/../post_build_step.rb"

module Plugins
  class JavadocPostBuildStep < Jenkins::PostBuildStep

    register 'Javadoc', 'Publish Javadoc'

    def dir(path)
      find(:path, path("javadocDir")).set path
    end
  end
end
