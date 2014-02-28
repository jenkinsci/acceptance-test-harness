module Plugins
  module JIRA
    class GlobalConfig < Jenkins::PageObject
      # Add a new JIRA site
      # TODO: this code doesn't work when there's already some existing sites configured
      def self.add(url,user,password)
        path = '/hudson-plugins-jira-JiraProjectProperty'
        find(:path,"#{path}/repeatable-add").click
        find(:path,"#{path}/sites/url").set(url)
        find(:path,"#{path}/sites/userName").set(user)
        find(:path,"#{path}/sites/password").set(password)
      end
    end
  end
end