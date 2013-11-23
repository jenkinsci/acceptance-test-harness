#
# Steps for interacting with local Git repository for tests
#

Given /^An empty test git repository$/ do
  @repo = Jenkins::Git::GitRepo.new
  @repo.init
  @cleanup << Proc.new do
    @repo.clean
  end
end

Given /^I check out code from the test Git repository$/ do
  step
end