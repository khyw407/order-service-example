#!groovy
node {
    def git
    def commitHash
    def buildImage

    stage('Checkout') {
        git = checkout scm
        commitHash = git.GIT_COMMIT
    }
    
    stage('Test') {
        try{
            sh './gradlew clean check'
        } finally {
            junit '**/build/test-results/**/*.xml'
        }
    }
    
    stage('Build') {
        sh './gradlew build -x test'
    }
    
    stage('Build Docker Image') {
        buildImage = docker.build("customer-service:latest")
    }
    
    stage('Archive') {
        parallel (
            "Archive Artifacts" : {
                archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
            },
            "Docker Image Push" : {
            	docker.withRegistry('https://localhost:5000') {
            		buildImage.push("latest")
            	}
            }
        )
    }
    
    stage('K8s Deploy'){
    	sh 'kubectl apply --namespace=development -f deployment.yml'
    }
}