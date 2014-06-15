package org.jenkinsci.test.acceptance.po

/**
 *
 * @author christian.fritz
 */
class UserListPage extends Page {

    static url = "securityRealm"
    static at = { title == "Users [Jenkins]" }
    static content = {
        userNames {
            $("table#people>tbody>tr:not(:first-child)>td:nth-child(2)>a")*.text()
        }

        configure {
            $("table#people>tbody>tr:not(:first-child)>td:nth-child(4)>a[href\$='configure']").collectEntries {
                def userName = it.attr("href").split("/")[-2]
                [userName, it]
            }
        }

        delete(to: DeleteUserPage) {
            $("table#people>tbody>tr:not(:first-child)>td:nth-child(4)>a[href\$='delete']").collectEntries {
                def userName = it.attr("href").split("/")[-2]
                [userName, it]
            }
        }
    }
}
