module Jenkins
  module Fixtures
    # Let Jenkins run inside this container
    class WinstoneDocker < Fixture.find("sshd")

      register "winstone_docker", [22,8080]

      # when running container, mount the entire host so that we can access jenkins.war anywhere
      # @docker_opts = "-v /:/root"
    end
  end
end
