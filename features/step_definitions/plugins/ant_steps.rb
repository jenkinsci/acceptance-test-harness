#!/usr/bin/env ruby

When /^I add an Ant build step for:$/ do |ant_xml|
  @job.configure do
    @job.add_script_step("cat > build.xml <<EOF
  #{ant_xml}
EOF")
    Plugins::Ant.add_ant_step('hello', 'build.xml')
  end
end
