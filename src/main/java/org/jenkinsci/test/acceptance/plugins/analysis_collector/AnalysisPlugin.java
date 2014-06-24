package org.jenkinsci.test.acceptance.plugins.analysis_collector;

/**
* @author Michael Prankl
*/
public enum AnalysisPlugin {
    CHECKSTYLE("Checkstyle") {
        @Override
        public void check(AnalysisCollectorFreestyleBuildSettings publisher, boolean checked) {
            publisher.isCheckstyleActivated.check(checked);
        }
    },
    DRY("Duplicate Code") {
        @Override
        public void check(AnalysisCollectorFreestyleBuildSettings publisher, boolean checked) {
            publisher.isDryActivated.check(checked);
        }
    },
    PMD("PMD") {
        @Override
        public void check(AnalysisCollectorFreestyleBuildSettings publisher, boolean checked) {
            publisher.isPmdActivated.check(checked);
        }
    },
    FINDBUGS("FindBugs") {
        @Override
        public void check(AnalysisCollectorFreestyleBuildSettings publisher, boolean checked) {
            publisher.isFindbugsActivated.check(checked);
        }
    },
    TASKS("Open Tasks") {
        @Override
        public void check(AnalysisCollectorFreestyleBuildSettings publisher, boolean checked) {
            publisher.isOpenTasksActivated.check(checked);
        }
    },
    WARNINGS("Compiler Warnings") {
        @Override
        public void check(AnalysisCollectorFreestyleBuildSettings publisher, boolean checked) {
            publisher.isWarningsActivated.check(checked);
        }
    };

    private String name;

    AnalysisPlugin(final String name) {
        this.name = name;
    }

    public abstract void check(final AnalysisCollectorFreestyleBuildSettings settings, final boolean checked);

    public String getName() {
        return name;
    }
}
