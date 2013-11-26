require 'spec_helper'
require 'jenkins/plugins/ant'

Jenkins.rspec "Ant plugin" do
  it 'allows users to use Ant in a freestyle project' do
    @jenkins.plugin_manager.install_plugin! 'ant'
    j = @jenkins.create_job('FreeStyle')
    j.configure do
      j.add_shell_step(<<eos)
cat > build.xml << EOF
<project default="hello">
  <target name="hello">
    <echo message="Hello World"/>
  </target>
</project>
EOF
eos
      j.add_build_step('Ant').tap do |s|
        s.target = 'hello'
      end
    end

    j.queue_build.succeeded?.should eql(true), "\nConsole output:\n#{j.last_build.console}\n\n"
  end
end