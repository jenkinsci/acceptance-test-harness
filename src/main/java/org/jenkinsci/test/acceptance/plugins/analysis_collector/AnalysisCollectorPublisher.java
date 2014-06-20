package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginBuildSettings;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Michael Prankl
 */
@Describable("Publish combined analysis results")
public class AnalysisCollectorPublisher extends AbstractCodeStylePluginBuildSettings {

    public final Control isCheckstyleActivated = control("isCheckStyleActivated");
    public final Control isDryActivated = control("isDryActivated");
    public final Control isPmdActivated = control("isPmdActivated");
    public final Control isOpenTasksActivated = control("isOpenTasksActivated");
    public final Control isWarningsActivated = control("isWarningsActivated");
    public final Control isFindbugsActivated = control("isFindbugsActivated");


    public AnalysisCollectorPublisher(Job parent, String path) {
        super(parent, path);
    }

    /**
     * Select if the warnings of given plugin should be collected by Analysis Collector Plugin.
     *
     * @param plugin  the Plugin
     * @param checked true or false
     */
    public void checkCollectedPlugin(AnalysisPlugin plugin, boolean checked) {
        plugin.check(this, checked);
    }

    public enum AnalysisPlugin {
        CHECKSTYLE("Checkstyle") {
            @Override
            public void check(AnalysisCollectorPublisher publisher, boolean checked) {
                publisher.isCheckstyleActivated.check(checked);
            }
        },
        DRY("Duplicate Code") {
            @Override
            public void check(AnalysisCollectorPublisher publisher, boolean checked) {
                publisher.isDryActivated.check(checked);
            }
        },
        PMD("PMD") {
            @Override
            public void check(AnalysisCollectorPublisher publisher, boolean checked) {
                publisher.isPmdActivated.check(checked);
            }
        },
        FINDBUGS("FindBugs") {
            @Override
            public void check(AnalysisCollectorPublisher publisher, boolean checked) {
                publisher.isFindbugsActivated.check(checked);
            }
        },
        TASKS("Open Tasks") {
            @Override
            public void check(AnalysisCollectorPublisher publisher, boolean checked) {
                publisher.isOpenTasksActivated.check(checked);
            }
        },
        WARNINGS("Compiler Warnings") {
            @Override
            public void check(AnalysisCollectorPublisher publisher, boolean checked) {
                publisher.isWarningsActivated.check(checked);
            }
        };

        private String name;

        AnalysisPlugin(final String name) {
            this.name = name;
        }

        public abstract void check(final AnalysisCollectorPublisher publisher, final boolean checked);

        public String getName() {
            return name;
        }
    }

}
