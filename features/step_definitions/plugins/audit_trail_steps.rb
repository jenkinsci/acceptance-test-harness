# It takes a couple of seconds for the plugin to start recording and displaying events.
# It is supposed to be ready after this step.
Given /^I have set up the Audit Trail plugin$/ do
  step 'I have installed the "audit-trail" plugin'
  @auditTrail = Plugins::AuditTrail.new @base_url
end

Then /^the audit trail should be empty$/ do
  @auditTrail.should be_empty
end

Then /^the audit trail should contain event "(.*?)"$/  do |event|
  @auditTrail.events.should include event
end
