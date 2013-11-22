module Plugins
  module JIRA
    class JIRA
      # @param docker [Jenkins::Docker::Container] container that runs JIRA
      def initialize(docker)
        @docker = docker
      end

      # block until JIRA becomes ready
      def wait_for_ready
        print "Waiting for JIRA to be ready... "
        STDOUT.flush

        start = Time.now.to_i
        Capybara.current_session.tap do |c|
          while true
            c.visit "http://localhost:#{@docker.port(2990)}/jira/"
            if c.title =~ /System Dashboard/
              puts " done"
              return
            end
            sleep 1
          end
        end
      end
    end
  end
end
