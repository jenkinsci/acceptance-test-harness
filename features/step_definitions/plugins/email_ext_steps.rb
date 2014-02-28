When /^I configure editable email "(.*?)" for "(.*?)"$/  do |subject, recipient, body|
  emailext = @job.add_postbuild_step 'Email-ext'
  emailext.subject = subject
  emailext.recipient = recipient
  emailext.body = body
end
