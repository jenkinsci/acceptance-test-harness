When /^I upload script "([^"]+)"$/ do |local_path|
  @scriptler = Jenkins::Scriptler::Page.new(@base_url)
  @script = @scriptler.upload_script_from local_path
end

When /^I create script$/ do |script|
  @scriptler = Jenkins::Scriptler::Page.new(@base_url)
  name = Jenkins::PageObject.random_name
  @script = @scriptler.create_script name, script
end

When /^I delete script "([^"]+)"$/ do |id|
  @scriptler.get_script(id).delete
end

When /^I run the script$/ do
  @script.run
end

When /^I run the script on ([^"]+)/ do |node|
  @script.run on: transform(node)
end

When /^I run parameterized script with:$/  do |table|
  @script.run with: table.rows_hash
end

When /^I add script parameters:$/  do |table|
  @script.set_parameters table.rows_hash
end


Then /^the script output should match "([^"]+)"$/ do |output|
  @script.output.should match /#{Regexp.escape(output)}/
end

Then /^script "([^"]+)" (should|should not) exist$/ do |id, should_or_not|
  @scriptler.get_script(id).send should_or_not, exist
end

Then /^the script output on (.+?) should match "([^"]+)"$/ do |node, output|
  @script.output.should include "\n[#{transform(node)}]:\n#{output}"
end

Then /^the script should not be run on (.+?)$/ do |node|
  @script.output.should_not include "\n[#{transform(node)}]:\n"
end

def transform(node_specifier)

  return node_specifier unless node_specifier =~ /^(master|all slaves|all nodes)$/

  if node_specifier == 'all nodes'
    node_specifier = 'all'
  end

  return "(#{node_specifier})"
end
