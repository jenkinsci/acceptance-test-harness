# Mix-in for JenkinsController that watches the log output by Jenkins
class LogWatcher
  TIMEOUT = ENV.fetch('STARTUP_TIMEOUT', 100).to_i

  # Launches a thread that monitors the given +pipe+ for log output and copy them over to +log+
  # @arg [IO]   pipe
  #       Raw console output from Jenkins
  # @arg [IO]   log
  #       Log output from Jenkins that's read from 'pipe' gets written here.
  def initialize(pipe,log,opts={})
    pattern = opts[:pattern] || /: Completed initialization/
    silent = opts[:silent] || false
    @ready = false
    @log_regex = nil
    @log_found = false
    @logged_lines = Array.new

    @log = log
    @pipe = pipe
    Thread.new do
      @line_count = 0
      while (line = @pipe.gets)
        log_line(line)

        next if @ready

        # earlier version of Jenkins doesn't have this line
        # if line =~ /INFO: Jenkins is fully up and running/
        if line =~ pattern
          puts " Jenkins completed initialization" unless silent
          @ready = true
        else
          unless silent
            print '.' if (@line_count%5)==0
            @line_count+=1
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

      if has_logged? /java.net.BindException: Address already in use/
        raise JenkinsController::RetryException.new "Port conflict detected"
      end
    end

    if @ready!=expected
      raise expected ? "Could not bring up a Jenkins server" : "Shut down of Jenkins server had timed out"
    end
  end

  def log_line(line)
    @log.write(line)
    @log.flush
    @logged_lines.push line

    unless @log_regex.nil?
      if line.match(@log_regex)
        @log_found = true
      end
    end
  end

  def wait_until_logged(regex, timeout=60)

    @log_regex = regex

    return if has_logged? regex

    start = Time.now.to_i

    while (Time.now.to_i - start) < timeout
      if @log_found
        @log_regex = nil
        @log_found = false
        return
      end
      sleep 1
    end

    raise TimeoutException.new "Pattern '#{regex.source}' was not logged within #{timeout} seconds"
  end

  class TimeoutException < Exception
  end

  # nil if not logged, matchdata of last record otherwise
  def has_logged?(regex)
    for line in @logged_lines.reverse
      match = line.match regex
      return match if !match.nil?
    end
    return nil
  end

  def close
    if @pipe
      @pipe.close
      @pipe = nil
    end
  end

  # Get log as string
  def full_log
    @logged_lines.join ''
  end
end
