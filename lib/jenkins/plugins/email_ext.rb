require 'jenkins/post_build_step'

module Plugins
  class EmailExtPublisher < Jenkins::PostBuildStep

    register 'Email-ext', 'Editable Email Notification'

    def subject=(subject)
      control("project_default_subject").set subject
    end

    def recipient=(recipient)
      control("project_recipient_list", "recipientlist_recipients").set recipient

      ensure_advanced_opened
      # check we really want to send it to the recipients
      control('project_triggers/sendToList').check
    end

    def body=(body)
      control("project_default_content").set body
    end

    private
    @advanced_opened = false
    def ensure_advanced_opened
      return if @advanced_opened
      control('advanced-button').click
      @advanced_opened = true
    end
  end
end
