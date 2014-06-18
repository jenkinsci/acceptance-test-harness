package org.jenkinsci.test.acceptance.plugins.analysis_collector;

/**
* @author Michael Prankl
*/
public enum AnalysisPlugin {
    CHECKSTYLE("Checkstyle") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isCheckstyleActivated.check(checked);
        }
    },
    DRY("Duplicate Code") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isDryActivated.check(checked);
        }
    },
    PMD("PMD") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isPmdActivated.check(checked);
        }
    },
    FINDBUGS("FindBugs") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isFindbugsActivated.check(checked);
        }
    },
    TASKS("Open Tasks") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isOpenTasksActivated.check(checked);
        }
    },
    WARNINGS("Compiler Warnings") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isWarningsActivated.check(checked);
        }
    };

    private String name;

    AnalysisPlugin(final String name) {
        this.name = name;
    }

    public abstract void check(final AnalysisCollectorPluginArea pluginArea, final boolean checked);

    public String getName() {
        return name;
    }
}
