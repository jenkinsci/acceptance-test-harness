// This pipeline runs on infra.ci.jenkins.io (private) to build, test and deploy the Docker image
buildDockerAndPublishImage('ath', [
  automaticSemanticVersioning: false,
  gitCredentials: 'jenkinsci-ath-ghapp', // Only available in infra.ci.jenkins.io
  dockerfile: 'src/main/resources/ath-container/Dockerfile',
  imageDir: 'src/main/resources/ath-container/',
  registryNamespace: 'jenkins',
])
