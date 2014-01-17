module Plugins
  module SCP
    class GlobalConfig < Jenkins::PageObject
      # Add a new SCP site
      # TODO: this code doesn't work when there's already some existing sites configured
      def self.add(host,port,remote_root,user,password,privatekey)
        path = '/be-certipost-hudson-plugin-SCPRepositoryPublisher'
        find(:path,"#{path}/repeatable-add").click
        find(:path,"#{path}/site/hostname").set(host)
        find(:path,"#{path}/site/port").set(port)
        find(:path,"#{path}/site/rootRepositoryPath").set(remote_root)
        find(:path,"#{path}/site/username").set(user)
        find(:path,"#{path}/site/password").set(password)
        find(:path,"#{path}/site/keyfile").set(privatekey)
      end
    end

    class Publisher < Jenkins::PostBuildStep
      register 'Publish artifacts to SCP Repository', 'Publish artifacts to SCP Repository'

      def add(source, destination)
        control('repeatable-add').click()
        control('entries/sourceFile').set(source)
        control('entries/filePath').set(destination)
      end
    end
  end
end
