module Jenkins
  module Fixtures
    # Represents a server that runs Tomcat7
    class Tomcat7 < Fixture

      # Tomcat's URL
      # @return [String]
      def url
        "http://localhost:#{port(8080)}"
      end

      register "tomcat7", [8080]
    end
  end
end
