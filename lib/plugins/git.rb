module Jenkins
  module Git
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
      rescue Capybara::ElementNotFound => ex # git 2.0
        localBranch = add_behaviour 'Check out to specific local branch'
        localBranch.name = branch
      end

      def local_dir(dir)
        advanced
        find(:path, path("relativeTargetDir")).set(dir)
      rescue Capybara::ElementNotFound => ex # git 2.0
        subDir = add_behaviour 'Check out to a sub-directory'
        subDir.path = dir
      end

      def repo_name(repo_name)
        remote_advanced
        find(:path, path("userRemoteConfigs/name")).set(repo_name)
      end

      def add_behaviour(title)
        return Jenkins::Git::Behaviour.add(self, title)
      end

      private
      def advanced
        find(:path, path("advanced-button")).click
      end

      def remote_advanced
        find(:path, path("userRemoteConfigs/advanced-button")).click
      end
    end

    class Behaviour
      include Jenkins::PageArea

      def self.add(git, title)
        addButton = find(:path, git.path('hetero-list-add[extensions]'))
        addButton.click
        click_link title

        type = get title
        return type.new(git, git.path('extensions'))
      end

      @@types = Hash.new

      # Register SCM type
      def self.register(title)
        raise "#{title} already registered" if @@types.has_key? title

        @@types[title] = self
      end

      # Get type by title
      def self.get(title)
        return @@types[title] if @@types.has_key? title

        raise "Unknown #{self.name.split('::').last} type #{title}. #{@@types.keys}"
      end
    end

    class LocalBranchBehaviour < Behaviour

      register 'Check out to specific local branch'

      def name=(branchName)
        find(:path, path('localBranch')).set branchName
      end
    end

    class LocalDirBehaviour < Behaviour

      register 'Check out to a sub-directory'

      def path=(path)
        find(:path, path('relativeTargetDir')).set path
      end
    end
  end
end
