pipeline {
    agent { 
    	docker {
            image 'docker.becpg.fr:443/becpg/becpg-ci-runner:latest'
            args '-u jenkins --privileged  -v /var/run/docker.sock:/var/run/docker.sock' 
        }
    }
    stages {
        stage('build') {
            steps {
                sh 'cd becpg-enterprise && mvn -B -DskipTests clean package'
            }
        }
        stage('test') {
            steps {
                sh 'MAVEN_OPTS="-Xms512m -Xmx2G" mvn clean test -P purge'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('integration-test') {
            steps {
                sh 'MAVEN_OPTS="-Xms512m -Xmx2G" mvn verify -Prun,integration-test,purge'
            }
            post {
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }
     
    }
}
