# Mix-in for JenkinsController that watches the log output by Jenkins
class LogWatcher
  TIMEOUT = 60

  # Launches a thread that monitors the given +pipe+ for log output and copy them over to +log+
  # @arg [IO]   pipe
  # @arg [IO]   log
  # @arg [Regexp]   pattern
  def initialize(pipe,log,pattern=/INFO: Completed initialization/)
    @ready = false
    @log_regex = nil
    @log_found = false

    @log = log
    @pipe = pipe
    Thread.new do
      while (line = @pipe.gets)
        log_line(line)
        # earlier version of Jenkins doesn't have this line
        # if line =~ /INFO: Jenkins is fully up and running/
        if line =~ pattern
          puts " Jenkins completed initialization"
          @ready = true
        else
          unless @ready
            print '.'
            STDOUT.flush
          end
        end
      end
      @ready = false
    end
  end

  # block until Jenkins is up and running
  def wait_for_ready(expected=true)
    start_time = Time.now
    while @ready!=expected && ((Time.now - start_time) < TIMEOUT)
      sleep 0.5
    end

    if @ready!=expected
      raise expected ? "Could not bring up a Jenkins server" : "Shut down of Jenkins server had timed out"
    end
  end

  def log_line(line)
    @log.write(line)
    @log.flush

    unless @log_regex.nil?
      if line.match(@log_regex)
        @log_found = true
      end
    end
  end

  def wait_until_logged(regex, timeout=60)
    start = Time.now.to_i
    @log_regex = regex

    while (Time.now.to_i - start) < timeout
      if @log_found
        @log_regex = nil
        @log_found = false
        return true
      end
      sleep 1
    end

    return false
  end

  def close
    @pipe.close if @pipe
  end
end
