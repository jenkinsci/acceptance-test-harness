require File.dirname(__FILE__) + "/lib/base"
require File.dirname(__FILE__) + "/pageobjects/newjob"
require File.dirname(__FILE__) + "/pageobjects/pluginmanager"


class NoopTest < JenkinsSeleniumTest
  def test_home
    go_home
  end
end
