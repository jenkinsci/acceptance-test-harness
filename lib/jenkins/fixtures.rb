require 'jenkins/docker'

module Jenkins
  module Fixtures

    # Fixture classes add fixture specific methods on top of the standard Container class
    class Fixture < Jenkins::Docker::Container
      # maps fixture name to the fixture class
      @@types = {}

      @@search_path = [ File.dirname(__FILE__)+"/../fixtures" ]

      # directories to search for test fixtures.
      # @return [Array<String>]
      def self.search_path
        @@search_path
      end


      # @return [Array<Integer>]  ports that are exposed from this container
      def self.ports
        @ports || self.superclass.ports
      end

      # Name of this fixture, which maps to the directory under fixtures/
      # @return [String]
      def self.name
        @name
      end

      # root directory of this fixture
      # @return [String]
      def self.dir
        @dir
      end

      # @return [String]
      def dir
        self.class.dir
      end

      def self.dir=(v)
        @dir=v
      end

      # Start a new container of this fixture
      #
      # @param name [String]    name of the fixture to start
      # @return [Fixture]       a running fixture instance
      def self.start(name)
        # try to load a fixture if it hasn't been
        @@search_path.each do |path|
          dir = "#{path}/#{name}"
          next if !Dir.exists? dir

          n = "#{dir}/#{name}.rb"
          if File.exists?(n)
            require n
          end

          t = @@types[name]
          t.dir = dir

          img = t.build()
          return t.new(img.start(t.ports).cid)
        end
      end

      # used by subtypes to register the fixture type by the fixture name to enable instance type selection
      def self.register(name,ports)
        @ports = ports
        @name = name
        @@types[name] = self
      end

      # build this fixture and returns an image.
      # this is mainly a hook to allow subtypes to override the build process
      #
      # @return [Docker::Image]         a prepared docker image
      def self.build()
        # TODO: if we are to do caching of images this is where we'd do it
        image_name = "jenkins/#{name}"
        Docker.build(image_name,dir)
      end
    end
  end
end
