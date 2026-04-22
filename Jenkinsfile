pipeline {
    agent any

    tools {
        jdk "jdk-21"
        maven "maven-3.9"
    }

    options {
        skipStagesAfterUnstable()
        timestamps()
    }

    stages {
        stage('Info') {
            steps {
                echo 'This build is running with the following tool versions.'
                sh 'java --version'
                sh 'git --version'
                sh 'mvn -version'
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Static Code Analysis') {
            steps {
                sh 'mvn -B verify -DskipTests'
            }
            post {
                always {
                    // SpotBugs
                    recordIssues enabledForFailure: true, tools: [
                        spotBugs(pattern: '**/target/spotbugsXml.xml')
                    ]

                    // PMD
                    recordIssues enabledForFailure: true, tools: [
                        pmdParser(pattern: '**/target/pmd.xml')
                    ]

                    // Checkstyle
                    recordIssues enabledForFailure: true, tools: [
                        checkStyle(pattern: '**/target/checkstyle-result.xml')
                    ]

                    // HTML Reports (if exists)
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site',
                        reportFiles: 'index.html',
                        reportName: 'Project Site'
                    ])
                }
            }
        }
    }

    post {
        success {
            echo '✅ ${env.JOB_NAME} - CI build completed successfully.'
        }
        failure {
            echo "❌ ${env.JOB_NAME} - CI build failed."
        }
    }
}
