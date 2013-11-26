%w(local_controller log_watcher).each { |f| require File.dirname(__FILE__)+"/"+f }

# Runs jenkins.war inside docker so that it gets a different IP address
# even though it's run on the same host.
#
# For efficiency, the docker container gets the entire host file system bind-mounted on it,
# and we ssh into that box and start jenkins.
class WinstoneDockerJenkinsController < LocalJenkinsController
  register :winstone_docker

  # @param [Hash] opts
  #     :war  => specify the location of jenkins.war
  def initialize(opts)
    super(opts)
  end

  # @return [Jenkins::Fixtures::WinstoneDocker]
  def container
    @container
  end

  def start_process
    @war = File.expand_path(@war) # turn into absolute path
    
    @container = Jenkins::Fixtures::Fixture.find("winstone_docker").start! "-v #{@tempdir}:/work -v #{File.dirname(@war)}:/war"

    @process = @container.ssh_popen(["java","-DJENKINS_HOME=/work",
                         "-jar", "/war/#{File.basename(@war)}", "--ajp13Port=-1", "--controlPort=#@control_port",
                         "--httpPort=#@http_port"].join(' '))
  end

  def stop!
    TCPSocket.open(container.ip_address, @control_port) do |sock|
      sock.write("0")
    end
    Process.kill("INT",@pid)
    @process.close
    @container.clean
  end

  def url
    "http://#{container.ip_address}:#@http_port"
  end

  private
  def set_random_ports
    @http_port    = random_local_port
    @control_port = random_local_port
  end
end
