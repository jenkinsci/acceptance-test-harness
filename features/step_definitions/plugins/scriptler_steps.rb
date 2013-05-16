When /^I upload script "([^"]+)"$/ do |local_path|
  @scriptler = Jenkins::Scriptler::Page.new(@base_url)
  @script = @scriptler.upload_script_from local_path
end

When /^I delete script "([^"]+)"$/ do |id|
  @scriptler.get_script(id).delete
end

When /^I run the script$/ do
  @script.run
end

When /^I run the script with:$/  do |table|
  @script.run with: table.rows_hash
end

Then /^the script output should match "([^"]+)"$/ do |output|
  @script.output.should match /#{Regexp.escape(output)}/
end

Then /^script "([^"]+)" (should|should not) exist$/ do |id, should_or_not|
  @scriptler.get_script(id).send should_or_not, exist
  #.exists.send should_or_not, be
end
