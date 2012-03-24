require File.dirname(__FILE__) + "/lib/base"
require File.dirname(__FILE__) + "/pageobjects/newjob"
require File.dirname(__FILE__) + "/pageobjects/pluginmanager"


class BasicSanityTests < JenkinsSeleniumTest
  def test_proper_title
    go_home
    assert_not_nil @driver.title.match("\[Jenkins\]")
  end

  # The following dynamically generates a number of plugin install tests. The
  # entries in the Array can either be a single string which represents the
  # plugin's shortname in the update center *or* it can be an Array of
  # [<short>, <long>] names.
  #
  # This Array syntax will allow the tests to properly look for installation
  # messages in the logs for plugins like "disk-usage" which have a long name
  # of "Disk Usage"
  #
  # You shouldn't need to edit the method generation at all, you'll only need
  # to add the appropriate entries to the array below

  ["git", "github", "chucknorris", "warnings", "violations",
   "xunit", "cobertura", "checkstyle", ["disk-usage", "Disk Usage"],
   ["greenballs", "Green Balls"], "groovy", "grails", "gradle", "jobConfigHistory",
   "build-timeout", "backup", "dry", "jira", "sonar"].each do |plugin|

    name = plugin
    # Handle plugins whose short-names do not match their "full names"
    if plugin.instance_of? Array
      name = plugin[1]
      plugin = plugin[0]
    end

    define_method(:"test_install_#{plugin.gsub("-", "_")}_plugin") do

      manager = PluginManager.new(@driver, @base_url)
      manager.install_plugin(plugin)

      found = @controller.wait_until_logged(/Installation successful: #{name}/i)
      assert found, "Failed to verify that we properly installed the #{name} plugin"

      restart_jenkins
      manager.assert_installed(plugin)

    end

  end
end
