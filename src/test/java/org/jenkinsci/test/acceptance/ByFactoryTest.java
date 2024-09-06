package org.jenkinsci.test.acceptance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author ogondza.
 */
public class ByFactoryTest {
    private ByFactory by = new ByFactory();

    @Test
    public void formatXPath() throws Exception {
        // No args
        String XPATH = "//*[text() = 'foo']";
        assertEquals(XPATH, by.formatXPath(XPATH));

        // Unquoted, quoted and double quoted placeholder
        assertEquals(
                "//*[text() =\"D'oh!\"or text() = \"D'oh!\" or text() = \"D'oh!\"]",
                by.formatXPath("//*[text() = %s or text() = '%s' or text() = \"%s\"]", "D'oh!", "D'oh!", "D'oh!"));
        assertEquals(
                "//*[text() ='\"Quotes3\"'or text() = '\"Quotes1\"' or text() = '\"Quotes2\"']",
                by.formatXPath(
                        "//*[text() = %3$s or text() = '%1$s' or text() = \"%2$s\"]",
                        "\"Quotes1\"", "\"Quotes2\"", "\"Quotes3\""));
        assertEquals(
                "//*[text() =concat('40° 26', \"'\", ' 46\"', '')or text() = concat('40° 26', \"'\", ' 46\"', '') or text() = concat('40° 26', \"'\", ' 46\"', '')]",
                by.formatXPath("//*[text() = %1$s or text() = '%1$s' or text() = \"%1$s\"]", "40° 26' 46\""));

        // Part of larger quoted string
        assertEquals("'/path/'", by.formatXPath("'/%s/'", "path"));
    }
}
