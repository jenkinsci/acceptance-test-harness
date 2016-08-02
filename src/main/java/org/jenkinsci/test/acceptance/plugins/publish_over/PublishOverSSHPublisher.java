package org.jenkinsci.test.acceptance.plugins.publish_over;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author jenky-hm
 */
@Describable("Send build artifacts over SSH")
public class PublishOverSSHPublisher extends AbstractStep implements PostBuildStep {
    public PublishOverSSHPublisher(Job parent, String path) {
        super(parent, path);
    }

    public final Control addServer = control("repeatable-add");
    // a lot more controls under advanced
    public final Control advancedServer = control("advanced-button");

    public final Control delete = control("repeatable-delete");

    public Publishers setPublishers() {
        String p = last(by.xpath(".//div[@name='publishers'][starts-with(@path,'%s/publishers')]", getPath())).getAttribute("path");
        return new Publishers(getPage(), p);
    }

    public Publishers addPublishers() {
        String path = createPageArea("publishers", new Runnable() {
            @Override public void run() {
                addServer.click();
            }
        });
        return new Publishers(getPage(), path);
    }

    public class Publishers extends PageAreaImpl {
        public Publishers(PageObject parent, String path) {
            super(parent, path);
        }

        // a lot of new options behind advanced...
        public final Control advancedPublisher = control("advanced-button"); // Below dropdown list
        public final Control addTransferSet = control("repeatable-add");

        public TransferSet setTransferSet() {
            String p = last(by.xpath(".//div[@name='transfers'][starts-with(@path,'%s/transfers')]", getPath())).getAttribute("path");
            return new TransferSet(getPage(), p);
        }

        public TransferSet addTransferSet() {
            String path = createPageArea("transfers", new Runnable() {
                @Override public void run() {
                    addTransferSet.click();
                }
            });
            return new TransferSet(getPage(), path);
        }
    }

    public static class TransferSet extends PageAreaImpl {
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
