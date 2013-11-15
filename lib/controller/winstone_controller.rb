%w(local_controller log_watcher).each { |f| require File.dirname(__FILE__)+"/"+f }

# Runs jenkins.war on the same system with built-in Winstone container
class WinstoneJenkinsController < LocalJenkinsController
  register :local # backward compatibility
  register :winstone

  # @param [Hash] opts
  #     :war  => specify the location of jenkins.war
  def initialize(opts)
    super(opts)
    set_random_ports
  end

  def start_process
    IO.popen(["java",
      "-jar", @war, "--ajp13Port=-1", "--controlPort=#@control_port",
      "--httpPort=#@http_port","2>&1"].join(' '))
  end

  def start!
    super
  rescue RetryException => e
    print e.message
    STDOUT.flush
    set_random_ports
    start!
  end

  def stop!
    begin
      TCPSocket.open("localhost", @control_port) do |sock|
        sock.write("0")
      end
      @log_watcher.wait_for_ready false
    rescue => e
      puts "Failed to cleanly shutdown Jenkins #{e}: Jenkins log:"
      puts @log_watcher.full_log
      puts "Trace:\n  "+e.backtrace.join("\n  ")
      puts "Killing #@pid"
      Process.kill("KILL",@pid)
    end
  end

  def url
    "http://127.0.0.1:#@http_port"
  end

  private
  def set_random_ports
    @http_port    = random_local_port
    @control_port = random_local_port
  end
end
