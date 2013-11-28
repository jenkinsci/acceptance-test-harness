require 'jenkins/docker'

module Jenkins
  module Fixtures

    # Fixture classes add fixture specific methods on top of the standard Container class
    class Fixture < Jenkins::Docker::Container
      # maps fixture name to the fixture class
      @@types = {}

      @@search_path = [ File.expand_path(Jenkins::ROOTDIR+"/fixtures") ]

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

      # root directory of this fixture
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
      # @return [Jenkins::Docker::Fixture]       a running fixture instance
      def self.start(name)
        find(name).start!
      end

      # Start the fixture identified by 'self'
      # this method is always invoked with the right fixture subtype as the left-hand side value.
      #
      # @param opts [String] additional command line options for 'docker run'
      # @param cmd  [String] command to run inside the container. defaults to what Dockerfile specifies.
      #                      overriding this parameter allows the caller to control what gets launched.
      # @return [Jenkins::Docker::Fixture]       a running fixture instance
      def self.start!(opts="",cmd=nil)
        img = self.build()
        c = img.start(self.ports,"#{@docker_opts} #{opts}",cmd)
        return self.new(c.cid, c.pid, c.logfile)
      end

      # Loads a fixture class identified by the given name
      #
      # @param name [String]    name of the fixture to start
      # @return [Class<Jenkins::Docker::Fixture>]       a configured fixture class
      def self.find(name)
        # try to load a fixture if it hasn't been
        @@search_path.each do |path|
          puts "Searching #{path}"
          dir = "#{path}/#{name}"
          next if !Dir.exists? dir

          n = "#{dir}/#{name}.rb"
          if File.exists?(n)
            require n
          end

          t = @@types[name]
          t.dir = dir
          return t
        end

        raise "Fixture #{name} not found"
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
      # @return [Jenkins::Docker::Image]         a prepared docker image
      def self.build()
        if @img.nil?
          self.superclass.build() if self.superclass!=Fixture

          image_name = "jenkins/#{name}"
          @img = Docker.build(image_name,dir)
        end
        @img
      end
    end
  end
end
