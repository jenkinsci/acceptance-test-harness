package org.jenkinsci.test.acceptance.utils.keycloack;

import java.net.URL;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.openqa.selenium.WebDriver;
import jakarta.inject.Inject;

public class KeycloakUtils extends CapybaraPortingLayerImpl {

    @Inject
    public WebDriver driver;
    @Inject
    public ElasticTime time;

    public KeycloakUtils() {
        super(null);
    }

    public void open(URL url) {
        visit(url);
    }

    public void login(String user) {
        login(user, user);
    }

    public void login(String user, String passwd) {
        waitFor(by.id("username"), 5);
        find(by.id("username")).sendKeys(user);
        find(by.id("password")).sendKeys(passwd);
        find(by.id("kc-login")).click();
    }


    public User getCurrentUser(String keycloakUrl, String realm) {
        driver.get(String.format("%s/realms/%s/account", keycloakUrl, realm));

        waitFor(by.id("username"), 5);
        String username = find(by.id("username")).getDomProperty("value");
        String email = find(by.id("email")).getDomProperty("value");
        String firstName = find(by.id("firstName")).getDomProperty("value");
        String lastName = find(by.id("lastName")).getDomProperty("value");


        return new User(null /* id not available in this page*/, username, email, firstName, lastName);
    }

    public void logout(User user) {
        final String caption = user.getFirstName() + " " + user.getLastName();
        waitFor(by.button(caption), 5);
        clickButton(caption);
        waitFor(by.button("Sign out"));
        clickButton("Sign out");
    }

    public static class User {

        private final String id;
        private final String userName;
        private final String email;
        private final String firstName;
        private final String lastName;

        public User(String id, String userName, String email, String firstName, String lastName) {
            this.id = id;
            this.userName = userName;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getId() {
            return id;
        }

        public String getUserName() {
            return userName;
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}
