module Plugins
  class GradleBuildStep < Jenkins::BuildStep

    register 'Gradle', 'Invoke Gradle script'

    def file(name)
      find(:path, path("buildFile")).set(name)
    end

    def dir(name)
      find(:path, path("rootBuildScriptDir")).set(name)
    end

    def switches(switches)
      find(:path, path("switches")).set(switches)
    end

    def version(version)
      path = path("useWrapper[false]/gradleName")
      find(:xpath, "//select[@path='#{path}']/option[@value='#{version}']").select_option
    end

    def description(description)
      find(:path, path("description")).set(description)
    end

    def tasks(tasks)
      find(:path, path("tasks")).set(tasks)
    end
  end
end
