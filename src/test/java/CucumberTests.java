import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * @author Stephen Connolly
 */
@RunWith(Cucumber.class)
@CucumberOptions(format = "pretty", tags = "~@wip", features = "features")
public class CucumberTests {
}
