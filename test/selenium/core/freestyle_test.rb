require File.dirname(__FILE__) + "/../lib/base"
require File.dirname(__FILE__) + "/../pageobjects/newjob"
require File.dirname(__FILE__) + "/../pageobjects/job"
require File.dirname(__FILE__) + "/../pageobjects/newslave"
require File.dirname(__FILE__) + "/../pageobjects/slave"
require File.dirname(__FILE__) + "/../pageobjects/globalconfig"

class FreestyleJobTests < JenkinsSeleniumTest
  def setup
    super
    @job_name = "Selenium_Test_Job"
    NewJob.create_freestyle(@driver, @base_url, @job_name)
    @job = Job.new(@driver, @base_url, @job_name)
  end

  def test_svn_checkout
    @job.configure do
      # checkout some small project from SVN
      @job.setup_svn("https://svn.jenkins-ci.org/trunk/hudson/plugins/zfs/")
      # check workspace if '.svn' dir is present, if not, fail the job
      @job.add_build_step "if [ '$(ls .svn)' ]; then \n exit 0 \n else \n exit 1 \n fi"
      sleep 10
    end

    @job.queue_build
    @job.wait_for_build
    build = @job.build(1)
    #build should fail if the project is not checked out. 
    #TODO any better way how to check it? Check if all file from repo are present?
    assert build.succeeded?, "The build did not succeed!"

  end
end
