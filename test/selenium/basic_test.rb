require File.dirname(__FILE__) + "/lib/base"


class BasicSanityTests < JenkinsSeleniumTest
  def test_proper_title
    @driver.navigate.to @base_url
    assert_not_nil @driver.title.match("\[Jenkins\]")
  end
end
