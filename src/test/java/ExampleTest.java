import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Created by boris on 26.04.17.
 */
public class ExampleTest {

    @Test
    public void testSth() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String sth =  new BufferedReader(new InputStreamReader(classloader.getResourceAsStream("stageview_plugin/single_job.txt")))
                .lines().collect(Collectors.joining("\n"));
        System.out.println(sth);
    }
}
