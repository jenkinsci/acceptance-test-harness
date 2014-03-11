package ant;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.controller.Authenticator;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsProvider;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class JocTest {
    @Inject
    JenkinsController jocc;

    @Inject
    Jenkins joc;

    @Inject
    JenkinsProvider provider;

    @Inject
    Injector injector;

    @Inject
    SlaveProvider slave;

    @Test
    public void bigFamily() throws Exception{
        List<Jenkins> armyOfJEs = new ArrayList<>();
        for (int i=0; i<2; i++) {
            JenkinsController c = provider.get();
            c.start();
            armyOfJEs.add(new Jenkins(injector,c));
        }

        // Now I have all the Jenkins masters!

//        JenkinsOC jocp = new JenkinsOC(joc);
//        for (Jenkins je : armyOfJEs) {
//            jocp.hookUp(je);
//        }

        System.out.println(joc.getVersion());
        for (Jenkins je : armyOfJEs) {
            System.out.println(je.getVersion());

            SlaveController s = slave.get();
            s.install(je);
            s.start();
        }


        assert true; // end of test
    }

//    public static class ModuleImpl extends AbstractModule {
//        @Override
//        protected void configure() {
//            bind(JenkinsController.class).annotatedWith(Names.named("joc")).toProvider(JenkinsProvider.class).in(TestScope.class);
//        }
//    }
}
