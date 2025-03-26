# GitHub Actions CI/CD

## 1. Self-hosted runner on EC2

### Requirements 1st Instance

- Ubuntu Server 24.04
- t2.small
- 15 GB

### Setup self-hosted runner for GitHub Actions

- Run the runner using: `./run.sh`

## 2. SonarQube on separate EC2

### Requirements for 2nd Instance

- Ubuntu Server 24.04
- t2.medium
- 8 GB

### Connect to the Instance and follow the steps

1. Update the packages first

    ```bash
    sudo apt update
    ```

2. Install & Setup Docker

    ```bash
    sudo apt install docker.io
    sudo usermod -aG docker $USER
    newgrp docker

    sudo systemctl restart docker
    sudo systemctl status docker
    ```

3. Run SonarQube as a container using Docker

    ```bash
    docker run -d --name sonar -p 9000:9000 sonarqube:lts-community

    docker ps
    ```

    - Expose port 9000 and access SonarQube on http://instance-public-ip:9000

4. Open SonarQube on browser and use the following:
    - User: admin
    - Password: admin

5. Create Access Token in SonarQube
    - Now go to `Administration > Security > Users`
    - In Administator (admin) > go to Token
    - Provide a token name and Generate
    - Copy the Token and Put it as a Secret (`SONAR_TOKEN`) for GitHub Actions
