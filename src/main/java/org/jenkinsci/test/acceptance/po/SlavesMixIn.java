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

    public <S extends Slave> S get(Class<S> type, String name) {
        return  newInstance(type,jenkins,name);
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

        S s = newInstance(type, jenkins, name);

        // reasonable starting point values
        s.setExecutors(1);
        s.setRemoteFs("/tmp/"+name);

        return s;
    }
}
