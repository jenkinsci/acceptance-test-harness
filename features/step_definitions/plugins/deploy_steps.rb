When(/^I deploy "([^"]*)" to docker tomcat7 fixture at context path "([^"]*)"$/) do |war,path|
  step = @job.add_postbuild_step 'Deploy WAR'
  step.archive = war
  step.contextPath = path
  step.container = 'Tomcat 7.x'
  step.user = 'admin'
  step.password = 'tomcat'
  step.url = @docker['tomcat7'].url
end

When(/^docker tomcat7 fixture should show "([^"]*)" at "([^"]*)"$/) do |pattern, url|
  http = RestClient.get @docker['tomcat7'].url+url
  http.to_str.should match /#{pattern}/
end
