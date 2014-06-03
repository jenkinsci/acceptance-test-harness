package org.jenkinsci.test.acceptance.plugins.jacoco

import org.jenkinsci.test.acceptance.po.Build
import org.jenkinsci.test.acceptance.po.Page

/**
 * Pageobject for the jacoco result page.
 *
 * @author christian.fritz
 */
class JacocoResultPage extends Page {
    static content = {
        heading { $ "h2" }
        summaryCoverage {
            def line = $("#main-panel table.pane:not(.sortable)>tbody>tr:nth-child(2)>td")
            [
                    'instruction': line.getAt(1).attr('data').toDouble(),
                    'branch'     : line.getAt(2).attr('data').toDouble(),
                    'complexity' : line.getAt(3).attr('data').toDouble(),
                    'lines'      : line.getAt(4).attr('data').toDouble(),
                    'methods'    : line.getAt(5).attr('data').toDouble(),
                    'classes'    : line.getAt(6).attr('data').toDouble()
            ]
        }

        breakdownCoverageLine {
            $("#main-panel table.pane.sortable>tbody>tr:not(tr:first-child)").collectEntries {
                def children = it.children('td')
                [children.getAt(0).text(), [
                        'instruction': children.getAt(1).attr('data').toDouble(),
                        'branch'     : children.getAt(2).attr('data').toDouble(),
                        'complexity' : children.getAt(3).attr('data').toDouble(),
                        'lines'      : children.getAt(4).attr('data').toDouble(),
                        'methods'    : children.getAt(5).attr('data').toDouble(),
                        'classes'    : children.getAt(6).attr('data').toDouble()
                ]]
            }
        }
    }
    static at = { heading.text() == "JaCoCo Coverage Report" }

    String convertToPath(Object[] args) {
        (args[0] as Build).url.toString() + "jacoco/" + (args.size() > 1 ? args[1..-1]*.toString().join('/') : "")
    }
}
