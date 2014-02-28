#
# Assist diagnosis by responding to SIGQUIT and dump threads like JVM does
#
require 'pp'

def backtrace_for_all_threads(signame,f)
  f.puts "--- got signal #{signame}, dump backtrace for all threads at #{Time.now}"
  if Thread.current.respond_to?(:backtrace)
    Thread.list.each do |t|
      f.puts t.inspect
      PP.pp(t.backtrace.delete_if {|frame| frame =~ /^#{File.expand_path(__FILE__)}/},
            f) # remove frames resulting from calling this method
    end
  else
    PP.pp(caller.delete_if {|frame| frame =~ /^#{File.expand_path(__FILE__)}/},
          f) # remove frames resulting from calling this method
  end
end

Signal.trap(3) do
  backtrace_for_all_threads("QUIT",STDERR)
end