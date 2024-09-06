package org.jenkinsci.test.acceptance.utils.groovy;

import groovy.ui.Console;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Interactive Groovy console that's useful while developing a test.
 *
 * @author Kohsuke Kawaguchi
 */
public class InteractiveConsole {
    /**
     * Call this method at the point you want to drop into the Groovy console for exploration.
     *
     * @param caller
     *      Methods/fields of this object becomes accessible to the script as if they are built-in.
     *      Usually you want to pass in the instance of the test class.
     * @param args
     *      [name, value, name, value, ... ] pair array that defines additional variables accessible
     *      from the script. Useful to expose local variables in scope
     */
    public static void execute(Object caller, Object... args) {
        Console cons = new Console();
        cons.getConfig().setScriptBaseClass(ClosureScript.class.getName());
        cons.setVariable("delegate", caller);

        for (int i = 0; i < args.length; i += 2) {
            cons.setVariable(args[i].toString(), args[i + 1]);
        }

        cons.run();

        final Object lock = new Object();
        final boolean[] done = new boolean[1];

        synchronized (lock) {
            JFrame f = (JFrame) cons.getFrame();
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    synchronized (lock) {
                        done[0] = true;
                        lock.notify();
                    }
                }
            });

            // block until the swing app is done
            while (!done[0]) {
                try {
                    lock.wait();
                } catch (InterruptedException x) {
                    throw new AssertionError(x);
                }
            }
        }
    }
}
