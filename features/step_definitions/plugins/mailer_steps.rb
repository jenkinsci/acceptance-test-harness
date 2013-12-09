Given /^a default mailer setup$/ do
  @mailer = $jenkins.configure.mailer
  @mailer.setup_defaults
end

When /^I send test mail to "(.*?)"$/ do |recipient|
  @mailer.send_test_mail(recipient)
  sleep 3
  page.should have_content 'Email was successfully sent'
end

When /^I configure mail notification for "(.*?)"$/ do |recipients|
  step = @job.add_postbuild_step 'Mailer'
  step.recipients recipients
end

Then /^a mail message "(.*?)" for "(.*?)" should match$/ do |subject, recipients, body|
  message = $jenkins.configure.wait_for_cond(message: "Email not delivered in time") do
    @mailer.mail subject
  end

  message.subject.should match subject
  message.to.should be == recipients.split
  message.body.decoded.should match body
end
