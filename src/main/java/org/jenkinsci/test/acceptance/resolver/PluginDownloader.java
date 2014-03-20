package org.jenkinsci.test.acceptance.resolver;

import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.machine.Machine;
import org.jenkinsci.test.acceptance.Ssh;

/**
 * @author Vivek Pandey
 */
public class PluginDownloader implements JenkinsResolver{

    private final String pluginPath;

    /**
     * plugin format is PLUGIN_NAME:VERSION format. If VERSION is missing latest version of plugin in installed
     *
     * PLUGIN_NAME is name of the plugin without .hpi extension
     */
    public PluginDownloader(String plugin) {
        String[] nameAndVersion = plugin.split(":");
        if(nameAndVersion.length == 1){
            this.pluginPath = String.format("http://updates.jenkins-ci.org/latest/%s",santizePluginName(nameAndVersion[0]));
        }else{
            this.pluginPath = String.format("http://updates.jenkins-ci.org/download/plugins/%s/%s/%s", nameAndVersion[0],
                    nameAndVersion[1],santizePluginName(nameAndVersion[0]));
        }
    }

    @Override
    public void materialize(Machine machine, String path) {
        Ssh ssh = machine.connect();
        if(!JenkinsDownloader.remoteFileExists(ssh.getConnection(),path,null)){
            ssh.executeRemoteCommand("mkdir -p "+ FileUtils.dirname(path));
            ssh.executeRemoteCommand(String.format("wget -q -O %s %s",path, pluginPath));
        }
    }

    private String santizePluginName(String name){
        return name.endsWith(".hpi") ? name : name+".hpi";
    }

}
