module Jenkins
  class Configuration < PageObject
    attr_reader :job, :combination

    def initialize(job, combination)
      @job = job
      @combination = combination
      super(job.job_url, nil)
    end

    def build(number)
      return Jenkins::BuiltConfiguration.new(
          Jenkins::Build.new(@job.job_url, @job, number), self
      )
    end
  end
end
