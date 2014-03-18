package ant;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsProvider;
import org.jenkinsci.test.acceptance.junit.JenkinsAcceptanceTestRule;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 */
public class JocTest {
    @Rule
    public JenkinsAcceptanceTestRule env = new JenkinsAcceptanceTestRule();

    @Inject @Named("joc")
    JenkinsController jocc;

    Jenkins joc;

    @Inject @Named("masters")
    JenkinsProvider provider;

    @Inject
    Injector injector;

    @Inject
    SlaveProvider slave;

    @Before
    public void setUp() {
        //Workaround as Jenkins tries to verify on different port???
        //TODO: remove this code
        try {
            jocc.start();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        joc = new Jenkins(injector,jocc);
    }

    @Test
    public void bigFamily() throws Exception{
        List<JenkinsController> controllers = new ArrayList<>();
        for (int i=0; i<2; i++) {
            JenkinsController c = provider.get();
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

        final SlaveController s = slave.get();
        handleSlave(s.install(joc));
        s.start();

        for (final Jenkins je : armyOfJEs) {
            for(int i=0;i<10;i++){
                System.out.println(je.getVersion());
                final SlaveController slaveController = slave.get();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        slaveController.install(je);
                        s.start();
                    }
                }).start();
            }
        }

        assert true; // end of test
    }


    private void handleSlave(Future<Slave> slaveFuture) throws ExecutionException, InterruptedException {
        Slave s = slaveFuture.get();
        System.out.println("slave is online? "+s.isOnline());
    }

//    public static class ModuleImpl extends AbstractModule {
//        @Override
//        protected void configure() {
//            bind(JenkinsController.class).annotatedWith(Names.named("joc")).toProvider(JenkinsProvider.class).in(TestScope.class);
//        }
//    }
}
