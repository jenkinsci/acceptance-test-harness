require 'jenkins/pagearea'

module Jenkins
  class ToolInstaller
    include Jenkins::PageArea

    @@types = Hash.new

    # Register Tool installer
    def self.register(title)
      raise "#{title} already registered" if @@types.has_key? title

      @@types[title] = self
    end

    # Get type by title
    def self.get(title)
      return @@types[title] if @@types.has_key? title

      raise "Unknown #{self.name.split('::').last} type #{title}. #{@@types.keys}"
    end

    def name=(name)
      control('name').set name
    end

    def install_version=(version)
      control('properties/hudson-tools-InstallSourceProperty').check
      control('properties/hudson-tools-InstallSourceProperty/installers/id').select version
    end

    def home=(dir)
      control('properties/hudson-tools-InstallSourceProperty').uncheck
      control('home').set dir
    end
  end
end
