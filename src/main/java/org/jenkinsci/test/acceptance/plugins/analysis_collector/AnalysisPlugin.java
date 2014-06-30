package org.jenkinsci.test.acceptance.plugins.analysis_collector;

/**
 * @author Michael Prankl
 */
public enum AnalysisPlugin {
    CHECKSTYLE("checkstyle", "Checkstyle") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isCheckstyleActivated.check(checked);
        }
    },
    DRY("dry", "Duplicate Code") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isDryActivated.check(checked);
        }
    },
    PMD("pmd", "PMD") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isPmdActivated.check(checked);
        }
    },
    FINDBUGS("findbugs", "FindBugs") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isFindbugsActivated.check(checked);
        }
    },
    TASKS("tasks", "Open Tasks") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isOpenTasksActivated.check(checked);
        }
    },
    WARNINGS("warnings", "Compiler Warnings") {
        @Override
        public void check(AnalysisCollectorPluginArea pluginArea, boolean checked) {
            pluginArea.isWarningsActivated.check(checked);
        }
    };

    private String id;
    private String name;

    AnalysisPlugin(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public abstract void check(final AnalysisCollectorPluginArea pluginArea, final boolean checked);

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
