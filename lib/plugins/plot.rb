module Plugins
  module Plot
    class PostBuildStep < Jenkins::PostBuildStep

      register 'Plot', 'Plot build data'

      def group=(group)
        find(:path, path('plots/group')).set group
      end

      def title=(title)
        find(:path, path('plots/title')).set title
      end

      def source(type, path)
        find(:path, path('plots/series/file')).locate.set path
        find(:path, path("plots/series/fileType[#{type}]")).locate.set true
      end
    end
  end
end
