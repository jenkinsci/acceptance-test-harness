module Plugins
  class GradleBuildStep < Jenkins::BuildStep

    register 'Gradle', 'Invoke Gradle script'

    def file(name)
      control("buildFile").set(name)
    end

    def dir(name)
      control("rootBuildScriptDir").set(name)
    end

    def switches(switches)
      control("switches").set(switches)
    end

    def version(version)
      path = path("useWrapper[false]/gradleName")
      find(:xpath, "//select[@path='#{path}']/option[@value='#{version}']").select_option
    end

    def description(description)
      control("description").set(description)
    end

    def tasks(tasks)
      control("tasks").set(tasks)
    end
  end
end
