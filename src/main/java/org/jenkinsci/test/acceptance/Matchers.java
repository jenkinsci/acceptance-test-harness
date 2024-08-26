package org.jenkinsci.test.acceptance;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Login;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.User;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Hamcrest matchers.
 *
 * @author Kohsuke Kawaguchi
 */
public class Matchers {
    /**
     * Asserts that given text is shown on page.
     */
    public static Matcher<WebDriver> hasContent(final String content) {
        return hasContent(Pattern.compile(Pattern.quote(content)));
    }

    public static Matcher<WebDriver> hasContent(final Pattern pattern) {
        return new Matcher<WebDriver>("Text matching %s", pattern) {
            // text captured the time matchesSafely was executed
            private String pageText;

            @Override
            public boolean matchesSafely(WebDriver item) {
                pageText = CapybaraPortingLayerImpl.getPageContent(item);
                return pattern.matcher(pageText).find();
            }

            @Override
            public void describeMismatchSafely(WebDriver item, Description mismatchDescription) {
                mismatchDescription.appendText("was ")
                        .appendValue(item.getCurrentUrl())
                        .appendText("\n")
                        .appendValue(pageText);
            }
        };
    }

    /**
     * Matches that matches {@link WebDriver} when it has an element that matches to the given selector.
     */
    public static Matcher<WebDriver> hasElement(final By selector) {
        return new Matcher<WebDriver>("contains element that matches %s", selector) {
            @Override
            public boolean matchesSafely(WebDriver item) {
                try {
                    item.findElement(selector);
                    return true;
                } catch (NoSuchElementException ignored) {
                    return false;
                }
            }

            @Override
            public void describeMismatchSafely(WebDriver item, Description d) {
                d.appendText("was at ").appendValue(item.getCurrentUrl());
            }
        };
    }

    public static Matcher<WebDriver> hasURL(final URL url) {
        return new Matcher<WebDriver>("URL matching %s", url) {
            @Override
            public boolean matchesSafely(WebDriver item) {
                return item.getCurrentUrl().equals(url.toString());
            }

            @Override
            public void describeMismatchSafely(WebDriver item, Description mismatchDescription) {
                mismatchDescription.appendText("was ").appendValue(item.getCurrentUrl());
            }
        };
    }

    /**
     * For asserting that a {@link PageObject}'s top page has an action of the given name.
     */
    public static Matcher<PageObject> hasAction(final String displayName) {
        return new Matcher<PageObject>("contains action titled %s", displayName) {
            @Override
            public boolean matchesSafely(PageObject po) {
                try {
                    po.open();
                    po.find(by.xpath("//div[@id='tasks']/div/span/a/span[text()='%s'] | //div[@id='tasks']/div/a[text()='%s']", displayName, displayName)).getText();
                    return true;
                } catch (NoSuchElementException ignored) {
                    return false;
                }
            }

            @Override
            public void describeMismatchSafely(PageObject po, Description d) {
                d.appendValue(po.url).appendText(" does not have action: ").appendValue(displayName);
            }
        };
    }

    public static Matcher<String> containsRegexp(String regexp) {
        return containsRegexp(regexp, 0);
    }

    /**
     * Matches if a string contains a portion that matches to the regular expression.
     */
    public static Matcher<String> containsRegexp(final String regexp, int opts) {
        return containsRegexp(Pattern.compile(regexp, opts));
    }

    /**
     * Matches the specified formatted string.
     *
     * @param format A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args   Arguments referenced by the format specifiers in the format string.  If there are more arguments
     *               than format specifiers, the extra arguments are ignored.  The number of arguments is variable and
     *               may be zero.  The maximum number of arguments is limited by the maximum dimension of a Java array
     *               as defined by <cite>The Java&trade; Virtual Machine Specification</cite>. The behaviour on a {@code
     *               null} argument depends on the <a href="../util/Formatter.html#syntax">conversion</a>.
     * @return the matcher
     */
    public static org.hamcrest.Matcher<String> containsString(final String format, final Object... args) {
        return org.hamcrest.Matchers.containsString(String.format(format, args));
    }

    /**
     * Matches if a string contains a portion that matches to the regular expression.
     */
    public static Matcher<String> containsRegexp(final Pattern re) {
        return new Matcher<String>("Matches regexp %s", re.toString()) {
            @Override
            public boolean matchesSafely(String item) {
                return re.matcher(item).find();
            }
        };
    }

    public static Matcher<PageObject> pageObjectExists() {
        return new Matcher<PageObject>("Page object exists") {
            private @CheckForNull HttpURLConnection conn; // Store for later defect localization
            @Override
            public void describeMismatchSafely(PageObject item, Description desc) {
                desc.appendText(item.url.toString()).appendText(" does not exist");
            }

            @Override
            public boolean matchesSafely(PageObject item) {
                try {
                    conn = IOUtil.openConnection(item.url);
                    IOUtils.toByteArray(conn.getInputStream());
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        };
    }

    public static Matcher<PageObject> pageObjectDoesNotExist() {
        return new Matcher<PageObject>("Page object does not exist") {
            private @CheckForNull HttpURLConnection conn; // Store for later defect localization
            @Override
            public void describeMismatchSafely(PageObject item, Description desc) {
                desc.appendText(item.url.toString()).appendText(" does exist");
            }

            @Override
            public boolean matchesSafely(PageObject item) {
                try {
                    conn = IOUtil.openConnection(item.url);
                    return conn.getResponseCode() == 404;
                } catch (IOException e) {
                    return false;
                }
            }
        };
    }

    public static Matcher<Jenkins> hasLoggedInUser(final String user) {
        return new Matcher<Jenkins>("has logged in user %s", user) {
            @Override
            public boolean matchesSafely(final Jenkins jenkins) {
                final User currentUser = jenkins.getCurrentUser();
                // if the user is not logged, currentUser can be not null with a null id
                return currentUser != null && currentUser.id() != null && currentUser.id().equals(user);
            }

            @Override
            public void describeMismatchSafely(final Jenkins item, final Description desc) {
                desc.appendText(user + " is not logged in.");
            }
        };
    }

    public static Matcher<Login> loggedInAs(final String user) {
        return new Matcher<Login>("has logged in user %s", user) {
            @Override
            public boolean matchesSafely(final Login login) {
                final User currentUser = login.getJenkins().getCurrentUser();
                // if the user is not logged, currentUser can be not null with a null id
                return currentUser != null && currentUser.id() != null && currentUser.id().equals(user);
            }

            @Override
            public void describeMismatchSafely(final Login item, final Description desc) {
                desc.appendText(user + " is not logged in.");
            }
        };
    }

    public static Matcher<Login> hasInvalidLoginInformation() {
        return new Matcher<Login>("has invalid login information message") {
            @Override
            public boolean matchesSafely(final Login login) {
                try {
                    login.find(by.xpath("//div[contains(text(), '%s')]", "Invalid username or password"));
                    return true;
                } catch (NoSuchElementException e) {
                    return false;
                }
            }

            @Override
            public void describeMismatchSafely(final Login item, final Description desc) {
                desc.appendText("There is no invalid login message.");
            }
        };
    }

    public static Matcher<User> isMemberOf(final String group) {
        return new Matcher<User>(" is member of group %s", group) {
            @Override
            public boolean matchesSafely(final User user) {
                user.open();
                try {
                    List<WebElement> webElements = user.all(by.xpath("//ul/li"));
                    for (WebElement webElement : webElements) {
                        if (webElement.getText().equals(group)) {
                            return true;
                        }
                    }
                } catch (NoSuchElementException e) {
                    return false;
                }
                return false;
            }

            @Override
            public void describeMismatchSafely(final User item, final Description desc) {
                desc.appendText(item + " is not member of group " + group + ".");
            }
        };
    }

    public static Matcher<User> fullNameIs(final String fullName) {
        return new Matcher<User>(" full name is %s", fullName) {
            @Override
            public boolean matchesSafely(final User user) {
                if (user.fullName() != null) {
                    return user.fullName().equals(fullName);
                }
                return false;
            }

            @Override
            public void describeMismatchSafely(final User item, final Description desc) {
                desc.appendText(item + " full name is not " + fullName + ".");
            }
        };
    }

    public static Matcher<User> mailAddressIs(final String mail) {
        return new Matcher<User>(" mail address is %s", mail) {
            @Override
            public boolean matchesSafely(final User user) {
                if (user.mail() != null) {
                    return user.mail().equals(mail);
                }
                return false;
            }

            @Override
            public void describeMismatchSafely(final User item, final Description desc) {
                desc.appendText("mail address of " + item + " is not " + mail + ".");
            }
        };
    }

    public static Matcher<File> existingFile() {
        return new Matcher<File>("an existing file") {
            @Override
            public boolean matchesSafely(final File item) {
                return item.exists() && item.isFile();
            }

            @Override
            public void describeMismatchSafely(final File item, final Description desc) {
                desc.appendText("File does not exist " + item.getAbsolutePath());
            }
        };
    }

    public static final ByFactory by = new ByFactory();
}
