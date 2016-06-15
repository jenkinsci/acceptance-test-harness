package org.jenkinsci.test.acceptance.po;

/**
 * DumbSlave adapted to credentials plugin on version 2.0.7
 */
public class DumbSlave2 extends DumbSlave {
    public DumbSlave2(Jenkins j, String name) {
        super(j, name);
    }

    @Override
    public void setExecutors(int n) {
        control(by.input("_.numExecutors")).set(n);
    }

    public <T extends ComputerLauncher> T setLauncher(Class<T> type, Class caption) {
        findCaption(caption, new Resolver() {
            @Override protected void resolve(String caption) {
                launchMethod.select(caption);
            }
        });
        return newInstance(type, this, "/launcher");
    }
}
