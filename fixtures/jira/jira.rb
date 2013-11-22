module Jenkins
  module Fixtures
    # Represents the JIRA instance
    class JIRA < Fixture
      # block until JIRA becomes ready
      def wait_for_ready
        print "Waiting for JIRA to be ready... "
        STDOUT.flush

        start = Time.now.to_i
        Capybara.current_session.tap do |c|
          while true
            c.visit url
            if c.title =~ /System Dashboard/
              puts " done"
              return
            end
            sleep 1
          end
        end
      end

      # JIRA top URL
      # @return [String]
      def url
        "http://localhost:#{port(2990)}/jira/"
      end

      register "jira", [2990]
    end
  end
end
