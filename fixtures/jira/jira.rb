require 'jiraSOAP'

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

      # Create a new project on this JIRA
      # @param key [String]   all caps ID of this project, like 'JENKINS'
      # @param display_name [String]  human readable name of this project
      def create_project(key,display_name=nil)
        jira = soap()

        p = ::JIRA::Project.new()
        p.name = display_name || key
        p.key = key
        p.lead = "admin"

        jira.create_project_with_project p
      end

      # Create a new issue
      # @return [JIRA::Issue] newly created ticket. Its 'key' property returns the issue ID like JENKINS-12345
      def create_issue(key, summary="Test ticket", description="some description")
        jira = soap()

        issue = ::JIRA::Issue.new.tap do |i|
          i.project_name = key
          i.type_id = 1 # Bug
          i.priority_id = 1 # whatever
          i.summary = summary
          i.description = description
        end

        i = jira.create_issue_with_issue issue
      end

      # Create A JIRA SOAP client
      # @return [JIRA::JIRAService]
      def soap
        @soap if @soap
        @soap=::JIRA::JIRAService.new(url).tap { |x| x.login 'admin','admin'}
      end

      register "jira", [2990]
    end
  end
end
