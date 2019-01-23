pipeline {
    agent { 
    	docker {
            image 'becpg-ci-runner:latest'
            args '-v /opt/mvn_repository:/root/.m2' 
            
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
                sh 'mvn clean test -Dmaven.test.failure.ignore=true -P purge'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('integration-test') {
            steps {
                sh 'MAVEN_OPTS="-Xms512m -Xmx2G" mvn install -Prun,integration-test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
     
    }
}
