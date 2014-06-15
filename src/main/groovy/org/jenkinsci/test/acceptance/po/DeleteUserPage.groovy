package org.jenkinsci.test.acceptance.po

/**
 *
 * @author christian.fritz
 */
class DeleteUserPage extends Page {

    static at = {
        deleteForm.text().contains("Are you sure about deleting the user from Jenkins?")
    }
    static content = {
        deleteForm { $("#main-panel form[name='delete']") }
        yes(to: UserListPage) { $("span.submit-button button") }
    }
}
