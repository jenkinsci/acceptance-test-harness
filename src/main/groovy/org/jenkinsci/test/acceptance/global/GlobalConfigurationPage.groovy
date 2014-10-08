package org.jenkinsci.test.acceptance.global

import org.jenkinsci.test.acceptance.po.Page

/**
 * Created by eli on 10/6/14.
 */
class GlobalConfigurationPage extends Page {
    static url = 'configure'
    static at = { title == 'Configure System [Jenkins]' }
    static content = {
        saveButton { $('button', text: 'Save') }
        applyButton { $('button', text: 'Apply') }

        addArtifactoryButton {$('button', path: contains('Artifactory')) }
        artifactoryUrl(wait: true, required: false){ $('input', name: '_.artifactoryUrl')[0]}
        artifactoryUsername(wait: true, required: false){$('input', path: '/org-jfrog-hudson-ArtifactoryBuilder/artifactoryServer/deployerCredentials/username')[0]}
        artifactoryPassword(wait: true, required: false){$('input', path: '/org-jfrog-hudson-ArtifactoryBuilder/artifactoryServer/deployerCredentials/password')[0]}
        artifactoryTestConnectionButton(wait: true, required: false){$('button', text: 'Test Connection')[0]}

    }



}
