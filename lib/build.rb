#!/usr/bin/env ruby
# vim: tabstop=2 expandtab shiftwidth=2

class Build
  include Capybara::DSL
  def initialize(base_url, job, number)
    @base_url = base_url
    @job = job
    @number = number
  end

  def job
    @job
  end

  def number
    @number
  end

  def build_url
    @job.job_url + "/#{@number}"
  end

  def console
    @console ||= begin
      visit("#{build_url}/console")
      find(:xpath, "//pre").text
    end
  end

  def succeeded?
    @succeeded ||= begin
      visit(build_url)
      status_icon = find(:xpath, "//img[@src='buildStatus']")

      status_icon["tooltip"] == "Success"
    end
  end

  def failed?
    return !succeeded
  end
end
