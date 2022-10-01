package org.jenkinsci.test.acceptance.plugins.jabber;

import javax.inject.Inject;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Created by jenky-hm on 6/14/14.
 */
public class JabberGlobalConfig extends PageAreaImpl {

    @Inject
    JenkinsController controller;

    public final Control enable = control("enabled");

    @Inject
    public JabberGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-plugins-jabber-im-transport-JabberPublisher");
    }

    public EnabledConfig enableConfig(){
        enable.check();
        return new EnabledConfig(getPage(),"/hudson-plugins-jabber-im-transport-JabberPublisher/enabled");
    }

    public static class EnabledConfig extends PageAreaImpl {
        public EnabledConfig(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control jabberid = control("jabberId");
        public final Control jabberPassword = control("password");
        public final Control addMUC = control("repeatable-add");
        public final Control advanced = control("advanced-button");

        public MUCConfig addMUCConfig(){
            addMUC.click();
            return new MUCConfig(getPage(), "/hudson-plugins-jabber-im-transport-JabberPublisher/enabled/initialChats" );
        }

        public AdvancedConfig addAdvancedConfig(){
            advanced.click();
            return new AdvancedConfig(getPage(), "/hudson-plugins-jabber-im-transport-JabberPublisher/enabled");
        }

    }

    public static class MUCConfig extends PageAreaImpl {
        public MUCConfig(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control mucName = control("name");
        public final Control mucPassword = control("password");
        public final Control notificationsOnly = control("notificationOnly");
        public final Control mucDelete = control("repeatable-delete");


    }

    public static class AdvancedConfig extends PageAreaImpl {
        public AdvancedConfig(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control hostname = control("hostname");
        public final Control port = control("port");
        public final Control defaultIdSuffix = control("defaultIdSuffix");
        public final Control emailAsJabberId = control("emailAsJabberId");
        public final Control enableSASL = control("enableSASL");
        public final Control exposePresence = control("exposePresence");
        public final Control subscriptionMode = control("subscriptionMode");
        public final Control commandPrefix = control("commandPrefix");
        public final Control nickname = control("nickname");
        public final Control jenkinsLogin = control("jenkinsLogin");
        public final Control useProxy = control("useProxy");
    }


    // path="/hudson-plugins-jabber-im-transport-JabberPublisher/enabled/name"
}
