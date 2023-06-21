/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.docker.fixtures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.utils.IOUtil;

@DockerFixture(id = "mailhog", ports = {1025, 8025})
public class MailhogContainer extends DockerContainer {
    public String getSmtpHost() {
        return ipBound(1025);
    }

    public int getSmtpPort() {
        return port(1025);
    }

    public void assertMail(final Pattern expectedSubject, String recipient, Pattern body) {
        JsonNode jsonMessages = fetchJsonMessages();
        for (JsonNode jsonMessage : jsonMessages) {
            JsonNode headers = jsonMessage.get("Content").get("Headers");
            String subject = headers.get("Subject").asText();
            if (expectedSubject.matcher(subject).find()) {
                assertEquals(recipient, headers.get("To").asText());
                assertThat(jsonMessage.get("Content").get("Body").toString(), containsRegexp(body));
            }
        }
    }

    private JsonNode fetchJsonMessages() {
        try {
            URL url = new URL("http://" + ipBound(8025) + ":" + port(8025) + "/api/v1/messages");
            HttpURLConnection c = IOUtil.openConnection(url);
            try {
                return new ObjectMapper().readTree(c.getInputStream());
            } finally {
                c.disconnect();
            }
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    /**
     * Checks that the mail has arrived.
     */
    public void assertMail(final Pattern subject, String recipient) {
        assertMail(subject, recipient, Pattern.compile(".*"));
    }
}
