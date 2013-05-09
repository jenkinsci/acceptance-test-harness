# Mix-in for JenkinsController that watches the log output by Jenkins
class LogWatcher
  TIMEOUT = 60

  # Launches a thread that monitors the given +pipe+ for log output and copy them over to +log+
  # @arg [IO]   pipe
  #       Raw console output from Jenkins
  # @arg [IO]   log
  #       Log output from Jenkins that's read from 'pipe' gets written here.
  # @arg [Regexp]   pattern
  def initialize(pipe,log,pattern=/INFO: Completed initialization/)
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
        # earlier version of Jenkins doesn't have this line
        # if line =~ /INFO: Jenkins is fully up and running/
        if line =~ pattern
          puts " Jenkins completed initialization"
          @ready = true
        else
          unless @ready
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

    return if has_logged regex

    start = Time.now.to_i

    while (Time.now.to_i - start) < timeout
      if @log_found
        @log_regex = nil
        @log_found = false
        return
      end
      sleep 1
    end

    raise "Pattern '#{regex.source}' was not logged within #{timeout} seconds"
  end

  def has_logged(regex)
    line_index = @logged_lines.index { |l| l.match regex }
    return !line_index.nil?
  end

  def close
    if @pipe
      @pipe.close
      @pipe = nil
    end
  end
end
