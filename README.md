# GitHub Actions CI/CD

## Overview

This guide provides a step-by-step approach to setting up a complete CI/CD pipeline using GitHub Actions, Self-Hosted Runners, SonarQube, and AWS EKS with Terraform. The pipeline automates compilation, security checks, unit testing, code quality analysis, Dockerization, and deployment to Kubernetes.

## Architecture

To efficiently manage and deploy the pipeline, we use three AWS EC2 instances:

- **Self-Hosted GitHub Actions Runner** (Runs CI/CD pipeline jobs)
- **SonarQube Server** (Code Quality & Security Analysis)
- **EKS Deployment Server** (Hosts AWS CLI, Terraform, and kubectl)

---

## 1. Self-hosted runner on EC2

### Requirements for 1st Instance

- Ubuntu Server 24.04
- t2.small
- 15 GB

Commands to run after Launching the Instance:

```bash
sudo apt update
sudo apt install -y docker.io git unzip curl wget
```

### Setup self-hosted runner for GitHub Actions

1. Go to your "Project Repo" on GitHub

2. Then go to `Settings > Actions > Runners > New self-hosted runner`

3. Select the Runner image. In this case use `Linux`, `x64` Architecture.

4. Follow the commands instructions on the EC2 instance:

5. Set the `runs-on` property of GitHub Action workflow yaml file:

    ```yaml
    runs-on: self-hosted
    ```

**NOTE**: Run the self-hosted runner using: `./run.sh`

---

## 2. SonarQube on separate EC2

### Requirements for 2nd Instance

- Ubuntu Server 24.04
- t2.medium
- 8 GB

Commands to run after Launching the Instance:

```bash
sudo apt update
sudo apt install -y docker.io git unzip curl wget
```

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

    - Expose port 9000 and access SonarQube on `http://<instance-public-ip>:9000`

4. Open SonarQube on browser and use the following:
    - User: admin
    - Password: admin

5. Create an Access Token in SonarQube
    - Now go to `Administration > Security > Users`
    - In Administator (admin) > go to Token
    - Provide a token name and Generate
    - Copy the Token and Put it as a **Repository Secret** (`SONAR_TOKEN`) for GitHub Actions

6. Set `SONAR_HOST_URL` as a **Repository Variable**: `http://<instance-public-ip>:9000`

7. If you stop the server then remember to start the container again:

    ```bash
    docker start sonar
    ```

---

## 3. Project Containerization on 1st EC2 Instance

### Setup Docker

1. Install Docker on Ubuntu from [Official Docs](https://docs.docker.com/engine/install/ubuntu/)

    ```bash
    for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do sudo apt-get remove $pkg; done

    # Add Docker official GPG key
    sudo apt-get update
    sudo apt-get install ca-certificates curl
    sudo install -m 0755 -d /etc/apt/keyrings
    sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    sudo chmod a+r /etc/apt/keyrings/docker.asc

    # Add the repository to Apt sources
    echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update

    # To install the latest version
    sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    ```

2. Provide permission to non-root user

    ```bash
    sudo usermod -aG docker $USER
    newgrp docker

    sudo systemctl restart docker
    sudo systemctl status docker
    ```

3. Verify installation

    ```bash
    docker version
    ```

### Setup Docker Authentication on GitHub Repo

1. Set a Repository Variable: `DOCKERHUB_USERNAME`, provide your Username

2. Set a Repository Secret: `DOCKERHUB_TOKEN`

    - Go to [DockerHub](https://app.docker.com/)
    - `User Icon > Account settings > Personal access tokens > Generate New Token`
    - Provide **Access token description**, set **Expiry**, set **Access Permission** to `Read, Write, Delete`
    - Generate the new token. Copy and Paste it as a Repo Secret on GitHub

---

## 4. Infrastructure  Management Server on EC2

### Requirements for 3rd Instance

- Ubuntu Server 24.04
- t2.small
- 8 GB

Commands to run after Launching the Instance:

```bash
sudo apt update
sudo apt install -y docker.io git unzip curl wget
```

### Setup AWS CLI

1. Install AWS CLI from [Official Docs](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)

    ```bash
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    unzip awscliv2.zip
    sudo ./aws/install
    ```

2. Configure AWS CLI:

    ```bash
    aws configure
    # Provide the AWS Access Key ID, Secret Access Key, Default region and format json

    aws configure list
    ```

3. Setup the following AWS Secrets as Repository Secret for GitHub Actions

    - AWS_ACCESS_KEY_ID
    - AWS_SECRET_ACCESS_KEY

### Setup Terraform

1. Install Terraform from [Official HashiCorp Docs](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli) > Linux

    ```bash
    sudo apt-get update && sudo apt-get install -y gnupg software-properties-common

    # Install the HashiCorp GPG Key:
    wget -O- https://apt.releases.hashicorp.com/gpg | \
    gpg --dearmor | \
    sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg > /dev/null

    # Verify the key's fingerprint:
    gpg --no-default-keyring \
    --keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg \
    --fingerprint

    # Add the official HashiCorp repository to your system
    echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] \
    https://apt.releases.hashicorp.com $(lsb_release -cs) main" | \
    sudo tee /etc/apt/sources.list.d/hashicorp.list

    # Download the package information from HashiCorp
    sudo apt update

    # Install Terraform from the new repository
    sudo apt-get install terraform
    ```

2. Verify installation on a new terminal:

    ```bash
    terraform -help
    ```

3. Either clone [My GitLab Repo](https://gitlab.com/devops-section/DevOps-learning/-/tree/main/terraform/eks-terraform) to get the EKS setup code

    ```bash
    # On your EC2 Server
    git clone https://gitlab.com/devops-section/DevOps-learning.git
    cd DevOps-learning/terraform/eks-terraform
    ```

4. OR you can Copy the particular 3 files (`main.tf`, `variables.tf`, `output.tf`) from Local device to EC2 instance

    ```bash
    # At first create a folder in EC2 instance
    mkdir -p $HOME/eks-terraform

    # Then from local device
    cd path/to/the/folder   # where the terraform code is stored locally
    scp -i /home/username/.ssh/SecOps-key.pem -r ./main.tf ubuntu@<instance-ip>:/home/ubuntu/eks-terraform

    # Same way copy the other files to the EC2 instance folder
    ```

5. Create the EKS Infrastructure

    ```bash
    # Connect to your EC2 instance
    cd $HOME/eks-terraform

    terraform init
    terraform apply

    # If you need to delete all the created resource
    terraform destroy
    ```

### Setup Kubectl

1. Install kubectl from [Kubernetes Official Docs](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/) on Linux

    ```bash
    # Download the latest release with the command:
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"


    # Validate the binary (optional)
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"
    echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check

    # Install kubectl
    sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

    # Test to ensure the version you installed is up-to-date
    kubectl version --client
    ```

2. Configure kubectl to use EC2

    ```bash
    aws eks update-kubeconfig --region us-east-1 --name supersection-cluster
    ```

3. Verify connection

    ```bash
    kubectl get nodes
    ```

4. Copy the kubeconfig and set it as a Repository Secret (`EKS_KUBECONFIG`)

    ```bash
    # Connect to the EC2
    cat $HOME/.kube/config

    # Copy this config file and paste it as a Secret
    ```

---

## Conclusion

🎯 With this setup, we now have:

- ✅ Automated CI/CD with GitHub Actions
- ✅ Security Scans with Trivy & Gitleaks
- ✅ Code Quality Checks with SonarQube
- ✅ Containerized Deployment with Docker & Kubernetes
- ✅ Scalable Deployment on AWS EKS using Terraform

> 🚀 Next Steps: Integrate Prometheus & Grafana for monitoring! 🔥

---

## Author

- **Name**: [Soumo Sarkar](https://soumosarkar-portfolio.netlify.app/)
- **Email**: <soumosarkar.official@gmail.com>
