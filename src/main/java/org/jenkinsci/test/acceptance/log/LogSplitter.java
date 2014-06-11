package org.jenkinsci.test.acceptance.log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Receives logs from {@link LogListener} and distributes them to other {@link LogListener}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class LogSplitter implements LogListenable, LogListener {
    private final List<LogListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addLogListener(LogListener l) {
        listeners.add(l);
    }

    @Override
    public void removeLogListener(LogListener l) {
        listeners.remove(l);
    }

    public List<LogListener> getListeners() {
        return listeners;
    }

    @Override
    public void processLine(String line) throws IOException {
        for (LogListener l : listeners) {
            l.processLine(line);
        }
    }

    @Override
    public void processClose(Exception t) {
        for (LogListener l : listeners) {
            l.processClose(t);
        }
    }

    public void clear() {
        listeners.clear();
    }
}
