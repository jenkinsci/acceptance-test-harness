package org.jenkinsci.test.acceptance.plugins.publish_over_ssh;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Christoph Ponikwar
 */
@Describable("Send build artifacts over SSH")
public class PublishOverSSHPublisher extends PostBuildStep {
    public PublishOverSSHPublisher(Job parent, String path) {
        super(parent, path);
    }

    public final Control addServer = control("repeatable-add");
    // a lot more controls under advanced
    public final Control advancedServer = control("advanced-button");

    public final Control delete = control("repeatable-delete");

    private static int counter = 1;


    public Publishers setPublishers() {
        String p = last(by.xpath(".//div[@name='publishers'][starts-with(@path,'%s/publishers')]", path)).getAttribute("path");
        return new Publishers(page,p);
    }

    public Publishers addPublishers() {
        addServer.click();
        String p = last(by.xpath(".//div[@name='publishers["+ counter +"]'][starts-with(@path,'%s/publishers["+ counter +"]')]", path)).getAttribute("path");
        counter++;
        return new Publishers(page,p);
    }

    public class Publishers extends PageArea {
        public Publishers(PageObject parent, String path) {
            super(parent, path);
        }

        // a lot of new options behind advanced...
        public final Control advancedPublisher = control("advanced-button"); // Below dropdown list
        public final Control addTransferSet= control("repeatable-add");

        public TransferSet setTransferSet() {
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", path)).getAttribute("path");
            return new TransferSet(page,p);
        }

        public TransferSet addTransferSet() {
            addTransferSet.click();
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", path)).getAttribute("path");
            return new TransferSet(page,p);
        }

    }

    public static class TransferSet extends PageArea {
        public TransferSet(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control sourceFiles = control("sourceFiles");
        // remove path before actual file but all files must have this path then
        public final Control removePrefix = control("removePrefix");
        // folder below the global folder is created if not there
        public final Control remoteDirectory = control("remoteDirectory");
        // exec a command
        public final Control execCommand = control("execCommand");

        // more options for individual transfers
        public final Control advancedTransfer = control("advanced-button");

    }

}
