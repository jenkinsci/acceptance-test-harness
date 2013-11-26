require 'jenkins/post_build_step'

module Plugins
  class JavadocPostBuildStep < Jenkins::PostBuildStep

    register 'Javadoc', 'Publish Javadoc'

    def dir(path)
      control("javadocDir").set path
    end
  end
end
