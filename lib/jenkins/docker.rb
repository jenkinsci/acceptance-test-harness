#
# Wrapper around docker
#
require 'tempfile'

module Jenkins
  module Docker

    # command to invoke docker
    DOCKER = "sudo docker "

    # returns true-ish if docker is available in the system
    def self.available?
      system('which docker', :out => '/dev/null')
    end

    # build a fixture
    #
    # @param tag [String]     name to give to the created image
    # @param dir [String]     a directory that houses Dockerfile
    # @return [Jenkins::Docker::Image]         a prepared docker image
    def self.build(tag,dir)
      if !system("#{DOCKER} build -t #{tag} .", :chdir=>dir)
        raise "Failed to build container"
      end

      Image.new(tag)
    end

    # Container image, a template to launch virtual machines from.
    class Image
      # @param name [String]   name of the image
      def initialize(name)
        @name = name.chomp
      end

      # @param ports [Array<Integer>]   ports to expose
      # @param opts [String] additional command line options for 'docker run'
      # @param cmd  [String] command to run inside the container. defaults to what Dockerfile specifies.
      #                      overriding this parameter allows the caller to control what gets launched.
      # @return [Jenkins::Docker::Container]
      def start(ports, opts="", cmd=nil)
        opts += ports.map {|y| " -p 127.0.0.1::#{y}"}.join

        if cmd.nil? then
          cmd=""
        else
          cmd = " #{cmd}"
        end

        cid_file = tempfile('cid')

        log = tempfile('docker-log')

        pid = spawn("#{DOCKER} run -cidfile=#{cid_file} #{opts} #{@name}#{cmd}", :in => '/dev/null', :err =>[:child,:out], :out => log)

        sleep 1   # TODO: properly wait for either cidfile to appear or process to exit

        if File.exists?(cid_file)
          cid = ''
          loop do
            cid = File.read(cid_file)
            break if cid != ''
            sleep 0.5
          end
          # rename the log file to match the container name
          logfile = "#{Dir.tmpdir}/#{cid}.log"
          File.rename log, logfile
          puts "Launching Docker container #{cid}: logfile is at #{logfile}"

          Container.new(cid, pid, logfile)
        else
          dead = Process.waitpid(pid, Process::WNOHANG) == pid
          if dead
            raise "docker #{@name}#{cmd} died unexpectedly: #{File.read(log)}"
          else
            raise "docker #{@name}#{cmd} didn't leave cidfile and is still running. Huh?"
          end
        end
      end

      def to_s
        "Docker image #{@name}"
      end

      # create a new temporary file without letting Ruby delete it in the end
      private
      def tempfile(prefix)
        Tempfile.open(prefix) do |t|
          path = t.path
          t.unlink
          path
        end
      end
    end

    # Running container, a virtual machine
    class Container
      # @param cid [String]   container ID
      # @param proc [IO]      IO object connected to the process
      def initialize(cid, pid, logfile)
        @cid = cid
        @pid = pid
        @logfile = logfile
      end

      # Container ID
      # @return [String]
      def cid
        @cid
      end

      # Process ID of the docker command
      def pid
        @pid
      end

      # Where is the log file for this container?
      def logfile
        @logfile
      end

      # find the ephemeral port that the given container port is mapped to
      # @return [Integer]
      def port(n)
        IO.popen("#{DOCKER} port #{@cid} #{n}") do |io|   # this returns single line like "0.0.0.0:55326"
          out = io.gets
          raise "port #{n} is not mapped for container #{@cid}" if out.nil?
          out.split(":")[1].to_i
        end
      end

      # stop and remove any trace of the container
      def clean
        Process.kill "INT",@pid
        if !system("#{DOCKER} kill #{@cid}", :out => '/dev/null')
          raise "Failed to kill #{@cid}"
        end
        if !system("#{DOCKER} rm #{@cid}", :out => '/dev/null')
          raise "Failed to rm #{@cid}"
        end
      end

      # copy a files/folders from inside the container to outside
      def cp(from,to)
        if !system("#{DOCKER} cp #{@cid}:#{from} #{to}")
          raise "Failed to copy #{from} to #{to}"
        end
      end

      # provide details of this container
      # @return [Hash]   output from 'docker inspect'
      def docker_inspect
        IO.popen("#{DOCKER} inspect #{@cid}") do |io|
          JSON.parse(io.read)[0]
        end
      end

      # IP address of this container reachable through the bridge
      def ip_address
        docker_inspect['NetworkSettings']['IPAddress']
      end

      def to_s
        "Docker container #{@cid}"
      end

      def self.of(test,name)
        test.instance_variable_get('@docker')[name]
      end
    end
  end
end
