require 'jenkins/post_build_step'

module Plugins
  class PublishHtmlPostBuildStep < Jenkins::PostBuildStep

    register 'Publish HTML', 'Publish HTML reports'

    attr_accessor :lastReport

    def add_report(title, directory)
      control('repeatable-add').click

      newReport = HTMLReportConfiguration.new self, path('reportTargets')
      newReport.dir = directory
      newReport.title = title
      return @lastReport = newReport
    end
  end

  class HTMLReportConfiguration
    include Jenkins::PageArea

    def dir=(path)
      control("reportDir").set path
    end

    def index=(path)
      control("reportFiles").set path
    end

    def title=(title)
      control("reportName").set title
    end
  end
end
