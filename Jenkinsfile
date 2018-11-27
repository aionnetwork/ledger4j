pipeline {
	agent any
	stages {
		stage('Build') {
			sh "./gradlew build"
		}

		stage('Archive') {
			steps {
				archiveArtifacts artifacts: 'build/libs/*'
			}
		}
	}

	post {
		always {
			cleanWs()
		}
	}
}