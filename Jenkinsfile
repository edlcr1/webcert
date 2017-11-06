#!groovy

def buildVersion = "5.4.0.${BUILD_NUMBER}"
def commonVersion = "3.5.0.+"
def infraVersion = "3.5.0.+"
def logsenderBaseVersion = "5.4.0.*" // Star is needed as this is a regexp

stage('checkout') {
    node {
        git url: "https://github.com/sklintyg/webcert.git", branch: GIT_BRANCH
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        try {
            shgradle "--refresh-dependencies clean build npmInstall camelTest testReport sonarqube -PcodeQuality -PcodeCoverage -DgruntColors=false \
                  -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'

        }
    }
}

stage('deploy') {
    node {
        def logsenderVersion = util.latestVersion("se/inera/intyg/logsender/logsender", logsenderBaseVersion)
        util.run {
            ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false", logsender_version: "${logsenderVersion}"], \
                installation: 'ansible-yum', inventory: 'ansible/inventory/webcert/test', playbook: 'ansible/deploy.yml'
            util.waitForServer('https://webcert.inera.nordicmedtest.se/version.jsp')
        }
    }
}

stage('restAssured') {
   node {
       try {
           shgradle "restAssuredTest -DbaseUrl=http://webcert.inera.nordicmedtest.se/ \
                 -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
       } finally {
           publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'web/build/reports/tests/restAssuredTest', \
               reportFiles: 'index.html', reportName: 'RestAssured results'
       }
   }
}

stage('protractor') {
   node {
       try {
           sh(script: 'sed -i -r "s,(e.code === \'ECONNRESET\'),e.code === \'ECONNRESET\' || e.code === \'ETIMEDOUT\'," test/node_modules/selenium-webdriver/http/index.js') // NMT magic
//           sh(script: 'mkdir -p test/node_modules')
//           sh(script: 'rm -rf test/node_modules/webcert-testtools') // Without this, node does not always recognize that a new version is available.
//           sh(script: 'ln -s ../webcertTestTools test/node_modules/webcert-testtools')
           wrap([$class: 'Xvfb']) {
               shgradle "protractorTests -Dprotractor.env=build-server \
                     -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
           }
       } finally {
           publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'test/dev/report', \
               reportFiles: 'index.html', reportName: 'Protractor results'
       }
   }
}

stage('tag and upload') {
    node {
        shgradle "uploadArchives tagRelease -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
    }
}

stage('notify') {
    node {
        util.notifySuccess()
    }
}
