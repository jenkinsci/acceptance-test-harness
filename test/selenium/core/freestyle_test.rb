require File.dirname(__FILE__) + "/../lib/base"
require File.dirname(__FILE__) + "/../pageobjects/newjob"
require File.dirname(__FILE__) + "/../pageobjects/job"


class FreestyleJobTests < JenkinsSeleniumTest
  def setup
    super
    @job_name = "Selenium_Test_Job"
    NewJob.create_freestyle(@driver, @base_url, @job_name)
    @job = Job.new(@driver, @base_url, @job_name)
  end

  def test_disable_job
    go_home

    @job.configure do
      @job.disable
    end

    # Let's open up the job page and verify that Jenkins says it's disabled
    @job.open

    enable_button = @driver.find_element(:xpath, "//button[text()='Enable']")
    assert_not_nil enable_button, "Couldn't find the [Enable] button, guess we failed to disable the job!"
  end

  def test_exec_shell_build_step
    @job.configure do
      @job.add_build_step "ls"
    end

    @job.queue_build

    build = @job.build(1)

    assert build.succeeded, "The build did not succeed!"

    assert_not_nil build.console.match("\\+ ls"), "Could not verify that the script ran in the following console: #{build.console}"
  end

end
