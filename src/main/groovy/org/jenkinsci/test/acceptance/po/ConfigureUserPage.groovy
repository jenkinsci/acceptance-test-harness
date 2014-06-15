package org.jenkinsci.test.acceptance.po

import org.kohsuke.randname.RandomNameGenerator

/**
 * Page object to change an user.
 *
 * @author christian.fritz
 */
class ConfigureUserPage extends Page {

    static at = { title.matches("User .* Configuration \\[Jenkins\\]") }
    static content = {
        fullName { $("input[path='/fullName']") }
        description { $("input[path='/description']") }
        showApiToken {
            $("button[path='/userProperty1/advanced-button']")
        }
        apiToken {
            if (showApiToken.isDisplayed()) showApiToken.click()
            $("input[path='/userProperty1/apiToken']")
        }
        changeApiToken {
            if (showApiToken.isDisplayed()) showApiToken.click()
            $("button[path='/userProperty1/validate-button']")
        }
        eMailAddress { $("input[name='email.address']") }
        defaultView { $("input[path='/userProperty4/primaryViewName']") }
        password { $("input[path='/userProperty5/password1']") }
        confirmPassword { $("input[path='/userProperty5/password2']") }
        sshPublicKeys { $("input[path='/userProperty6/authorizedKeys']") }
        caseSensitiveSearch { $("input[path='/userProperty7/insensitiveSearch']") }
        save { $("span.submit-button button") }
    }


    private static RandomNameGenerator randomNameGenerator = new RandomNameGenerator();

    private static def randName() {
        randomNameGenerator.next();
    }

    def changeFullName() {
        def name = randName()
        fullName.value(name)
        name
    }
}
