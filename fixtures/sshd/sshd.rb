module Jenkins
  module Fixtures
    # Represents a server with SSHD
    class SSHD < Fixture

      # SSH private key to talk to this host
      # @return [String]
      def private_key
        key = SSHD.dir+"/unsafe"    # 'self.dir' which might point to subtype
        File.chmod(0600,key)
        key
      end

      # ssh command line
      def ssh
        "ssh -p #{port(22)} -o StrictHostKeyChecking=no -i #{private_key} test@localhost"
      end

      # login with SSH public key and make sure you can connect
      def ssh_with_publickey(cmd)
        if !system("#{ssh} #{cmd}")
          raise "ssh failed!"
        end
      end

      def ssh_popen(cmd,&block)
        IO.popen("exec #{ssh} #{cmd} 2>&1",&block)
      end

      register "sshd", [22]
    end
  end
end
