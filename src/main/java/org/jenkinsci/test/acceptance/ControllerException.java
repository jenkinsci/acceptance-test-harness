package org.jenkinsci.test.acceptance;

import java.io.IOException;

/**
 * @author: Vivek Pandey
 */
public class ControllerException extends RuntimeException {
    public ControllerException(String message) {
        super(message);
    }

    public ControllerException(String format, Throwable e) {
        super(format, e);

    }
}
