#!/usr/bin/env ruby
require 'jenkins/pageobject'

module Plugins
  module Groovy
    class SystemBuildStep < Jenkins::BuildStep
      register 'System groovy', 'Execute system Groovy script'

      def command=(text)
        control('scriptSource[0]').check

        area_path = path('scriptSource[0]/command')
        textarea = find(:path, area_path, :visible => false)
        if textarea[:class].include? 'codemirror'
          codemirror = Jenkins::Util::CodeMirror.new(page, area_path)
          codemirror.set_content text
        else
          textarea.set text
        end
      end

      def file=(path)
        control('scriptSource[1]').check
        control('scriptSource[1]/scriptFile').set path
      end
    end

    class BuildStep < SystemBuildStep
      register 'Groovy', 'Execute Groovy script'

      def version=(version)
        control('groovyName').select version
      end
    end

    class Tool < Jenkins::ToolInstaller
      register 'Groovy'

      def initialize(global)
        super(global, '/hudson-plugins-groovy-GroovyInstallation/tool')
      end
    end
  end
end
