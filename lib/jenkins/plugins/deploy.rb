module Plugin
  module Deploy
    class Pubished < Jenkins::PostBuildStep
      register 'Deploy WAR', 'Deploy war/ear to a container'

      def archive=(archive)
        control('war').set archive
      end

      def contextPath=(contextPath)
        control('contextPath').set contextPath
      end

      def container=(container)
        control('').select container
      end

      def user=(user)
        control('adapter/userName').set user
      end

      def password=(password)
        control('adapter/password').set password
      end

      def url=(url)
        control('adapter/url').set url
      end
    end
  end
end
