%w(local_controller log_watcher).each { |f| require File.dirname(__FILE__)+"/"+f }

# Runs jenkins.war on the same system with built-in Winstone container
class WinstoneJenkinsController < LocalJenkinsController
  register :local # backward compatibility
  register :winstone

  # @param [Hash] opts
  #     :war  => specify the location of jenkins.war
  def initialize(opts)
    super(opts)
  end

  def start_process
    # Chose a random port, just to be safe
    @http_port    = random_local_port
    @control_port = random_local_port
    IO.popen(["java",
      "-jar", @war, "--ajp13Port=-1", "--controlPort=#@control_port",
      "--httpPort=#@http_port","2>&1"].join(' '))
  end

  def stop!
    begin
      TCPSocket.open("localhost", @control_port) do |sock|
        sock.write("0")
      end
      @log_watcher.wait_for_ready false
    rescue => e
      puts "Failed to cleanly shutdown Jenkins #{e}"
      puts "  "+e.backtrace.join("\n  ")
      puts "Killing #@pid"
      Process.kill("KILL",@pid)
    end
  end

  def url
    "http://127.0.0.1:#@http_port"
  end
end
