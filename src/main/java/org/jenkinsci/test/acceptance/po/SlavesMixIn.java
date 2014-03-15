package org.jenkinsci.test.acceptance.po;

/**
 * Mix-in for slaves.
 *
 * @author Kohsuke Kawaguchi
 */
public class SlavesMixIn extends MixIn {
    private final Jenkins jenkins;

    public SlavesMixIn(Jenkins jenkins) {
        super(jenkins);
        this.jenkins = jenkins;
    }

    public <S extends Slave> S create(Class<S> type) {
        return create(type,createRandomName());
    }

    /**
     * Creates a new slave of the given type. After the call, the web driver is on the config page.
     */
    public <S extends Slave> S create(Class<S> type, String name) {
        visit("computer/new");

        find(by.input("name")).sendKeys(name);
        find(by.xpath(".//INPUT[@type='radio'][@value='hudson.slaves.DumbSlave']")).click();
        clickButton("OK");
        // This form submission will drop us on the configure page

        try {
            S s = type.getConstructor(Jenkins.class, String.class).newInstance(jenkins, name);
            return s;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
