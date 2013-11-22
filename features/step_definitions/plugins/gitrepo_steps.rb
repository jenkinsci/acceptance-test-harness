#
# Steps for interacting with local Git repository for tests
#

Given /^an empty test git repository$/ do
  @repo = Jenkins::Git::GitRepo.new
  @repo.init
  @cleanup << Proc.new do
    @repo.clean
  end
end

When /^I check out code from the test Git repository$/ do
  step "I check out code from Git repository \"#{@repo.ws}\""
end

When(/^I commit "([^"]*)" to the test Git repository$/) do |msg|
  @repo.commit msg
end