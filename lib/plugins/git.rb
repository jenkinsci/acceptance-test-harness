module Jenkins
  class GitScm < Scm

    register 'Git'

    def url(url)
      find(:path, path("userRemoteConfigs/url")).set(url)
    end

    def branch(branch)
      find(:path, path("branches/name")).set(branch)
    end

    def local_branch(branch)
      advanced
      find(:path, path("localBranch")).set(branch)
    end

    def local_dir(dir)
      advanced
      find(:path, path("relativeTargetDir")).set(dir)
    end

    def repo_name(repo_name)
      remote_advanced
      find(:path, path("userRemoteConfigs/name")).set(repo_name)
    end

    private
    def advanced
      find(:path, path("advanced-button")).click
    end

    def remote_advanced
      find(:path, path("userRemoteConfigs/advanced-button")).click
    end
  end
end
