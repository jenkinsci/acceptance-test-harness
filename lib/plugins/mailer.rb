require 'capybara'
require 'capybara/dsl'
require 'mail'

module Plugins
  class MailerPostBuildStep < Jenkins::PostBuildStep

    register 'Mailer', 'E-mail Notification'

    def recipients(recipients)
      find(:path, path('mailer_recipients')).set recipients
    end
  end

  class Mailer
    include Jenkins::PageArea

    SERVER = 'mailtrap.io'
    MAILBOX = 'selenium-tests-69507a9ef0aa7fa5'
    PASSWORD = '72a80a49ae5ab81d'
    PORT = '2525'
    TOKEN = 'wvpGO0F4gT8DTZJIHzkpmQ'
    MESSAGES_API_URL = 'http://mailtrap.io/api/v1/inboxes/%s/messages?page=1&token=%s'
    MESSAGE_API_URL = 'http://mailtrap.io/api/v1/inboxes/%s/messages/%s?token=%s'

    def initialize(global_config, prefix)
      super(global_config, prefix)
      @global = global_config
      @fingerprint = "%s@%s.com" % [Jenkins::PageObject.random_name, MAILBOX]
    end

    def setup_defaults
      @global.configure do
        find(:path, path('smtpServer')).set SERVER
        find(:path, path('advanced-button')).click
        find(:path, path('useSMTPAuth')).check
        find(:path, path('useSMTPAuth/smtpAuthUserName')).set MAILBOX
        find(:path, path('useSMTPAuth/smtpAuthPassword')).set PASSWORD
        find(:path, path('smtpPort')).set PORT
        # Fingerprint to identify message sent from this test run
        find(:path, path('replyToAddress')).set @fingerprint
      end
    end

    def send_test_mail(recipient)
      @global.open
      find(:path, path('')).check
      find(:path, path('/sendTestMailTo')).set recipient
      find(:path, path('/validate-button')).click
    end

    def mail(subject)
      messages = []
      fetch_messages.each do |msg|
        if msg['message']['title'].match subject

          message = Mail.new fetch_message(msg['message']['id'])
          if is_ours? message
            messages << message
          end
        end
      end

      raise "More than one matching message" if messages.count > 1

      return messages[0]
    end

    def all_mails

      mids = []
      fetch_messages.each do |msg|
        mids << msg['message']['id']
      end

      messages = []
      mids.each do |mid|

        msg = Mail.new(fetch_message mid)
        if is_ours? msg
          messages << msg
        end
      end

      return messages
    end

    private
    # Use only messages with matching fingerprint
    def is_ours?(message)
      message.reply_to.include? @fingerprint
    end

    def fetch_messages
      fetch_json(MESSAGES_API_URL % [MAILBOX, TOKEN])
    end

    def fetch_message(message_id)
      fetch_json(MESSAGE_API_URL % [MAILBOX, message_id, TOKEN])['message']['source']
    end

    def fetch_json(url)
      return JSON.parse(Net::HTTP.get_response(URI.parse(url)).body)
    end
  end
end
