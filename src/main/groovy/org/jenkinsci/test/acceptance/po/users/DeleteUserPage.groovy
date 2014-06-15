package org.jenkinsci.test.acceptance.po.users

import org.jenkinsci.test.acceptance.po.Page

/**
 * PageObject for the delete user page.
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
