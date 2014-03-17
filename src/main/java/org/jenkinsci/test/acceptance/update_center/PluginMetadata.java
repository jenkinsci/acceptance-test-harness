package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.GAV;
import com.cloudbees.sdk.maven.RepositoryService;
import com.google.inject.Injector;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.apache.http.entity.ContentType.*;

/**
 * Databinding for installable plugin in UC.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginMetadata {
    public String name, version, gav;
    public List<Dependency> dependencies;
    public URL url;

    void init(UpdateCenterMetadata parent) {
        for (Dependency d : dependencies) {
            d.init(parent);
        }
    }

    public GAV getGAV() {
        String[] t = gav.split(":");
        return new GAV(t[0], t[1], t[2]);
    }

    public void uploadTo(Jenkins jenkins, Injector i) throws ArtifactResolutionException, IOException {
        RepositoryService rs = i.getInstance(RepositoryService.class);
        ArtifactResult r = rs.resolveArtifact(makeArtifact());

        HttpClient httpclient = HttpClientBuilder.create().build();

        HttpPost post = new HttpPost(jenkins.url("pluginManager/uploadPlugin").toExternalForm());
        HttpEntity e = MultipartEntityBuilder.create()
                .addBinaryBody("name", r.getArtifact().getFile(), APPLICATION_OCTET_STREAM, name + ".jpi")
                .build();
        post.setEntity(e);

        HttpResponse response = httpclient.execute(post);
        if (response.getStatusLine().getStatusCode()>=400)
            throw new IOException("Failed to upload plugin: "+response.getStatusLine()+"\n"+
                    IOUtils.toString(response.getEntity().getContent()));
    }

    private DefaultArtifact makeArtifact() {
        String[] t = gav.split(":");
        return new DefaultArtifact(t[0], t[1], "hpi", t[2]);
    }

    @Override
    public String toString() {
        return super.toString()+"["+name+","+version+"]";
    }
}
