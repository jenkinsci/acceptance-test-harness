When(/^I deploy "([^"]*)" to docker tomcat7 fixture at context path "([^"]*)"$/) do |war,path|
  section = find(:xpath, "//div[@descriptorId='hudson.plugins.deploy.DeployPublisher']")
  section.find(:path, '/publisher/war').set(war)
  section.find(:path, '/publisher/contextPath').set(path)

  section.find(:xpath,'.//option[@value="hudson.plugins.deploy.tomcat.Tomcat7xAdapter"]').click()

  # these parameters are hard-coded to tomcat7 docker fixture
  section.find(:path, '/publisher/adapter/userName').set("admin")
  section.find(:path, '/publisher/adapter/password').set("tomcat")
  section.find(:path, '/publisher/adapter/url').set(@docker['tomcat7'].url)

end
When(/^docker tomcat7 fixture should show "([^"]*)" at "([^"]*)"$/) do |pattern, url|
  http = RestClient.get @docker['tomcat7'].url+url
  http.to_str.should match /#{pattern}/
end