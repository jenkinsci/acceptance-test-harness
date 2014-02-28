require 'spec_helper'

Jenkins.rspec "Ant plugin" do
  it 'allows users to use Ant in a freestyle project' do
    @jenkins.plugin_manager.install_plugin! 'ant'
    j = @jenkins.create_job('FreeStyle')
    j.configure do
      j.add_create_file_step('build.xml',<<eos)
<project default="hello">
  <target name="hello">
    <echo message="Hello World"/>
  </target>
</project>
eos
      j.add_build_step('Ant').tap do |s|
        s.target = 'hello'
      end
    end

    j.queue_build.should_succeed
  end
end