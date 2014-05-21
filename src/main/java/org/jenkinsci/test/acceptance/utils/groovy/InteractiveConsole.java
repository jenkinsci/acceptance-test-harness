package org.jenkinsci.test.acceptance.utils.groovy;

import groovy.ui.Console;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
     */
    public static void execute(Object caller) throws InterruptedException {
        Console cons = new Console();
        cons.getConfig().setScriptBaseClass(ClosureScript.class.getName());
        cons.setVariable("delegate",caller);
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
            while (!done[0])
                lock.wait();
        }
    }
}
