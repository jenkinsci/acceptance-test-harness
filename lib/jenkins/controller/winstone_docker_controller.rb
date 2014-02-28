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
    @war = File.expand_path(File.readlink(@war)) # turn into absolute path
    fixture = @opts[:fixture]||"winstone_docker"  # fixture to use

    @container = Jenkins::Fixtures::Fixture.find(fixture).start! "-v #{@tempdir}:/work -v #{File.dirname(@war)}:/war"

    @process = @container.ssh_popen(["java","-DJENKINS_HOME=/work",
                         "-jar", "/war/#{File.basename(@war)}", "--ajp13Port=-1", "--controlPort=8081",
                         "--httpPort=8080","< /dev/null"].join(' '))
  end

  def stop!
    TCPSocket.open(container.ip_address, 8081) do |sock|
      sock.write("0")
    end
    Process.kill("INT",@pid)
    @process.close
    @container.clean
  end

  def url
    "http://#{container.ip_address}:8080"
  end
end
