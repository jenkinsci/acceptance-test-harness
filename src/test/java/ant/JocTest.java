package ant;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.controller.Authenticator;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsProvider;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class JocTest extends AbstractJUnitTest {
    @Inject @Named("joc")
    JenkinsController jocc;

    Jenkins joc;

    @Inject
    JenkinsProvider provider;

    @Inject
    Injector injector;

    @Inject
    SlaveProvider slave;

    @Before
    public void setUp() {
        joc = new Jenkins(injector,jocc);
    }

    @Test
    public void bigFamily() throws Exception{
        List<JenkinsController> controllers = new ArrayList<>();
        for (int i=0; i<2; i++) {
            JenkinsController c = provider.get();
//            c.start();    // we are making Provider<JenkinsController> responsible for this
            controllers.add(c);
        }

        List<Jenkins> armyOfJEs = new ArrayList<>();
        for (JenkinsController c : controllers) {
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
