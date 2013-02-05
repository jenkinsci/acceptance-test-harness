When /^I set the build timeout to (\d+) minutes$/ do |timeout|
  # Check the [x] Abort the build if it's stuck
  find(:xpath, "//input[@name='hudson-plugins-build_timeout-BuildTimeoutWrapper']").set(true)

  choose 'build-timeout.timeoutType'
  fill_in '_.timeoutMunites', :with => timeout
end
