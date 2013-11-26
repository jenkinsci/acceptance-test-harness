require 'jenkins/pagearea'

module Plugins
  module EnvInject
    class Step < Jenkins::BuildStep

      register 'Env Inject', 'Inject environment variables'

      def vars=(vars)
        control('propertiesContent').set vars
      end
    end
  end
end
