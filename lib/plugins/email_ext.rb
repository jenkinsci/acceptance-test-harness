require File.dirname(__FILE__) + "/../post_build_step.rb"

module Plugins
  class EmailExtPublisher < Jenkins::PostBuildStep

    register 'Email-ext', 'Editable Email Notification'

    def subject=(subject)
      control("project_default_subject").set subject
    end

    def recipient=(recipient)
      control("project_recipient_list").set recipient
    end

    def body=(body)
      control("project_default_content").set body
    end
  end
end
