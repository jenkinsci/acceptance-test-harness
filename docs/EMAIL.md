# Testing Emails
This harness has the `MailService` class to assist test scenarios that involve Jenkins sending out email and the test
script asserting their contents.

This class encapsulates where the mail server runs and how to retrieve email sent from Jenkins.

To obtain an instance of a properly configured `MailService`, just inject that into your test:

    @Inject
    MailService mail;

First, its `setup` method must be called to set up Jenkins global configuration correctly.
You can then interact with Jenkins to induce it to send out email, then you can use methods
on `MailService` to inspect and assert on email that have been sent to the server:

    void testFoo() {
        mail.setup(new MailerGlobalConfig(jenkins));
        ...
        mail.assertMail(
                Pattern.compile("^Modified "),
                "dev@example.com",
                Pattern.compile("\nwith amendment$"));
    }

See `MailerPluginTest` for a concrete example of how to write such tests.

## Using different Mailtrap account
The harness comes with the `Mailtrap` class that uses a shared account of the Jenkins project.
This account does allow multiple people to independently run tests without colliding with each other,
but beware that the contents of the email are visible to the world.

If for some reason this is problematic, you can create a separate account and use the wiring
script or additional `Module` to bind `MailService` to an instance of `Mailtrap`.

A similar mechanism allows you to replace Mailtrap with another service or your own email server.
