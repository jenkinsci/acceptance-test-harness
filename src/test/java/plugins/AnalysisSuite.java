package plugins;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for the static analysis plug-ins. This suite contains test for the following plug-ins:
 * <ul>
 *     <li>CheckStyle</li>
 *     <li>FindBugs</li>
 *     <li>PMD</li>
 *     <li>Warnings</li>
 *     <li>Task Scanner</li>
 *     <li>Analysis Collector</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
// TODO: Add tests for the dry plug-in
@RunWith(Suite.class)
@SuiteClasses({CheckStylePluginTest.class, FBPluginTest.class, PmdPluginTest.class, WarningsPluginTest.class,
        TaskScannerPluginTest.class, AnalysisCollectorPluginTest.class})
public class AnalysisSuite {
}
