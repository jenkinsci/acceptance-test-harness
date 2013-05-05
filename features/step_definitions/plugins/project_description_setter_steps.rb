When /^I setup project description from the file "([^"]*)" in workspace/ do |file_name|
  find(:path, "/org-jenkinsCi-plugins-projectDescriptionSetter-DescriptionSetterWrapper").set(true)
  find(:path, "/org-jenkinsCi-plugins-projectDescriptionSetter-DescriptionSetterWrapper/projectDescriptionFilename").set(file_name)
end
