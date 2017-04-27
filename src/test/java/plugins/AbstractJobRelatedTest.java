package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Base class for tests related to Jobs.
 * Contains several methods to create or control different types of jobs.
 *
 * @author Rene Zarwel
 */
public abstract class AbstractJobRelatedTest extends AbstractJUnitTest{

  /**
   * Simple interface to pass a job configuration.
   * @param <T> Job to configure.
   */
  public interface JobConfigurer<T extends Job> {
    void configure(T job);
    default void applyConfiguration(T job){
      job.configure();
      configure(job);
      job.save();
    }
  }

  /**
   * Builds a specific job on an defined owner and applies a configuration.
   *
   * @param jobClass Job to create.
   * @param configurer Configuration to apply.
   * @param owner Owner of Jobs.
   * @return Created an configured job.
   */
  public <T extends Job> T createAndConfigureJob(Class<T> jobClass, JobConfigurer<T> configurer, Container owner){
    T newJob = owner.getJobs().create(jobClass);
    configurer.applyConfiguration(newJob);
    return newJob;
  }

  /**
   * Builds a specific job and applies a configuration.
   *
   * @param jobClass Job to create.
   * @param configurer Configuration to apply.
   * @return Created an configured job.
   */
  public <T extends Job> T createAndConfigureJob(Class<T> jobClass, JobConfigurer<T> configurer){
    return createAndConfigureJob(jobClass, configurer, jenkins);
  }

  /**
   * Builds a specific job.
   *
   * @param jobClass Job to create.
   * @return Created an configured job.
   */
  public <T extends Job> T createJob(Class<T> jobClass){
    return createAndConfigureJob(jobClass, job -> {}, jenkins);
  }

  /**
   * Builds a Freestyle Job and applies a configuration.
   *
   * @param configurer Configuration to apply.
   * @return Created an configured freestyle job.
   */
  public FreeStyleJob createFreeStyleJob(JobConfigurer<FreeStyleJob> configurer){
    return createAndConfigureJob(FreeStyleJob.class, configurer);
  }

  /**
   * Builds a simple Freestyle Job.
   *
   * @return Created an configured freestyle job.
   */
  public FreeStyleJob createFreeStyleJob(){
    return createJob(FreeStyleJob.class);
  }

  /**
   * Builds a failing Freestyle Job.
   *
   * @return Created an configured freestyle job.
   */
  public FreeStyleJob createFailingFreeStyleJob(){
    return createFreeStyleJob(job -> job.addShellStep("exit 1"));
  }

  /**
   * Builds an unstable Freestyle Job.
   *
   * @return Created an configured freestyle job.
   */
  public FreeStyleJob createUnstableFreeStyleJob(){
    return createFreeStyleJob(job -> {
      String resultFileName = "status.xml";

      //TODO: Remove Hack if job can be set unstable directly.
      job.addShellStep(
            "echo '<testsuite><testcase classname=\"\"><failure>\n" +
            "</failure></testcase></testsuite>'>" + resultFileName
      );
      job.addPublisher(JUnitPublisher.class).testResults.set(resultFileName);
    });
  }

  /**
   * Build Job and wait until finished.
   *
   * @param job Job to build
   * @return The made build
   */
  public Build buildJobAndWait(final Job job) {
    return job.startBuild().waitUntilFinished();
  }

  /**
   * Builds the job and waits until the job has been finished. The build result must be SUCCESS.
   *
   * @param job the job to build
   * @return the successful build
   */
  public Build buildSuccessfulJob(final Job job) {
    return buildJobAndWait(job).shouldSucceed();
  }

  /**
   * Builds the job and waits until the job has been finished. The build result must be FAILURE.
   *
   * @param job the job to build
   * @return the failed build
   */
  protected Build buildFailingJob(final Job job) {
    return buildJobAndWait(job).shouldFail();
  }

  /**
   * Builds the job and waits until the job has been finished. The build result must be UNSTABLE.
   *
   * @param job the job to build
   * @return the unstable build
   */
  protected Build buildUnstableJob(final Job job) {
    return buildJobAndWait(job).shouldBeUnstable();
  }

}
