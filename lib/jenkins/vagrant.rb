require 'log4r'
require 'vagrant'
require 'vagrant/command'
require 'vagrant/cli'
require 'vagrant/util/platform'

# Monkey patching Vagrant
module Vagrant
  module Communication
    # Provides communication with the VM via SSH.
    class SSH
      # execute the command, echo back its output to console, and return its exit code
      def system_ssh(command,opts=nil)
        exit_status = execute(command,opts) do |type, data|
          # Determine the proper channel to send the output onto depending
          # on the type of data we are receiving.
          channel = type == :stdout ? :out : :error

          # Print the SSH output as it comes in, but don't prefix it and don't
          # force a new line so that the output is properly preserved
          @vm.ui.info(data.to_s,
                      :prefix => false,
                      :new_line => false,
                      :channel => channel)
        end
      end
    end
  end
end
