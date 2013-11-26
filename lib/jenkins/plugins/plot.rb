module Plugins
  module Plot
    class PostBuildStep < Jenkins::PostBuildStep

      register 'Plot', 'Plot build data'

      def group=(group)
        control('plots/group').set group
      end

      def title=(title)
        control('plots/title').set title
      end

      def source(type, path)
        control('plots/series/file').set path
        control("plots/series/fileType[#{type}]").set true
      end
    end
  end
end
