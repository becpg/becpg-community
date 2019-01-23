pipeline {
    agent { 
    	docker {
            image 'docker.becpg.fr:443/becpg/becpg-ci-runner:latest'
            args '-u jenkins --privileged -v /mnt/stateful_partition/mvn_reporitory:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock' 
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
                sh 'MAVEN_OPTS="-Xms512m -Xmx2G" mvn clean test -Dmaven.test.failure.ignore=true -P purge,ci'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('integration-test') {
            steps {
                sh 'MAVEN_OPTS="-Xms512m -Xmx2G" mvn install -Prun,integration-test,ci'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
     
    }
}
