#
# Wrapper around docker
#
module Jenkins::Docker

  # command to invoke docker
  DOCKER = "sudo docker "

  # returns true-ish if docker is available in the system
  def self.available?
    system('which docker', :out => '/dev/null')
  end

  # build a fixture
  #
  # @param name [String]    a directory name inside fixtures/ that designate the container to build
  # @return [Image]         a prepared docker image
  def self.build(name)
    image_name = "jenkins/#{name}"
    fixture = File.dirname(__FILE__)+"/../fixtures/#{name}"
    if !system("#{DOCKER} build -t #{image_name} .", :chdir=>fixture)
      raise "Failed to build container"
    end

    Image.new(image_name)
  end

  # Container image, a template to launch virtual machines from.
  class Image
    # @param name [String]   name of the image
    def initialize(name)
      @name = name.chomp
    end

    # @param ports [Array<Integer>]   ports to expose
    # @return [Container]
    def start(ports)
      opts = ports.map {|y| " -p 127.0.0.1::#{y}"}.join
      IO.popen("#{DOCKER} run -d #{opts} #{@name}") do |p|
        sleep 3   # TODO: find out how to properly wait for the service to start. maybe just wait for port to start listening?

        Container.new(p.gets)
      end
    end

    def to_s
      "Docker image #{@name}"
    end
  end

  # Running container, a virtual machine
  class Container
    def initialize(cid)
      @cid = cid.chomp
    end

    # Container ID
    # @return [String]
    def cid
      @cid
    end

    # find the ephemeral port that the given container port is mapped to
    # @return [Integer]
    def port(n)
      IO.popen("#{DOCKER} port #{@cid} #{n}") do |io|   # this returns single line like "0.0.0.0:55326"
        io.gets.split(":")[1].to_i
      end
    end

    # stop and remove any trace of the container
    def clean
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

    def to_s
      "Docker container #{@cid}"
    end
  end
end
