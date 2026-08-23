# Docker Network Address Pool Troubleshooting

## Problem

Docker Compose fails while creating a network:

```text
✘ Network docker-config_default Error
Error response from daemon: all predefined address pools have been fully subnetted

failed to create network docker-config_default:
Error response from daemon: all predefined address pools have been fully subnetted
```

This means Docker cannot allocate another subnet for a user-defined network.

This guide is intended for **native Docker Engine running inside WSL2**, 
without Docker Desktop.

---

## 1. Understand the Problem

Docker creates a network for each Docker Compose project by default.

For example:

```text
docker-config_default
project-a_default
project-b_default
...
```

Each user-defined bridge network requires its own subnet.

If Docker has exhausted the configured address pool, it cannot create another network and reports:

```text
all predefined address pools have been fully subnetted
```

The problem is usually caused by one of the following:

1. Too many unused Docker networks.
2. Too small Docker default address pools.
3. Many Compose projects creating separate networks.
4. Stale Docker network state.
5. A Docker daemon configuration with an unsuitable `default-address-pools` configuration.

---
# `wsl-ubuntu-docker-engine-setup.md`

# WSL2 Ubuntu + Native Docker Engine Setup

This guide describes how to completely reinstall Ubuntu in WSL2 and install
Docker Engine natively inside WSL.

The setup does **not** use Docker Desktop.

---

## 1. Uninstall Ubuntu from WSL

### 1.1 Check installed WSL distributions

Open **PowerShell** and run:

```powershell
wsl --list --verbose
````

Example:

```text
  NAME      STATE    VERSION
* Ubuntu    Running  2
```

### 1.2 Stop WSL

```powershell
wsl --shutdown
```

### 1.3 Unregister Ubuntu

> **Warning:** This permanently deletes the Ubuntu WSL distribution,
> including installed packages, Docker, configuration, and files stored
> inside the Ubuntu filesystem.

Run:

```powershell
wsl --unregister Ubuntu
```

Verify:

```powershell
wsl --list --verbose
```

Ubuntu should no longer be listed.

---

# 2. Install Ubuntu into WSL

### 2.1 Install Ubuntu

From **PowerShell as Administrator**:

```powershell
wsl --install -d Ubuntu
```

Wait until Ubuntu provisioning finishes.

If Windows asks for a reboot:

```powershell
Restart-Computer
```

After reboot, start Ubuntu:

```powershell
wsl -d Ubuntu
```

### 2.2 Complete Ubuntu setup

Ubuntu will ask you to create:

```text
Enter new UNIX username:
New password:
Retype new password:
```

Use your normal Linux username, for example:

```text
demo
```

### 2.3 Verify the installation

Inside Ubuntu:

```bash
cat /etc/os-release
```

Check WSL:

```powershell
wsl --list --verbose
```

Expected:

```text
NAME      STATE      VERSION
Ubuntu    Running    2
```

### 2.4 Verify systemd

Inside Ubuntu:

```bash
ps -p 1 -o comm=
```

Expected:

```text
systemd
```

If it returns:

```text
systemd
```

you can use `systemctl` to manage Docker.

---

# 3. Install Docker Engine

## 3.1 Update Ubuntu

Inside Ubuntu:

```bash
sudo apt update
sudo apt upgrade -y
```

Install prerequisites:

```bash
sudo apt install -y ca-certificates curl
```

## 3.2 Add Docker's official GPG key

```bash
sudo install -m 0755 -d /etc/apt/keyrings
```

```bash
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  -o /etc/apt/keyrings/docker.asc
```

```bash
sudo chmod a+r /etc/apt/keyrings/docker.asc
```

## 3.3 Add Docker repository

```bash
sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
```

Update package information:

```bash
sudo apt update
```

## 3.4 Install Docker Engine and Compose

```bash
sudo apt install -y \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin
```

This installs:

* Docker Engine
* Docker CLI
* containerd
* Docker Buildx
* Docker Compose V2

## 3.5 Start Docker

Because WSL uses systemd:

```bash
sudo systemctl enable docker
```

```bash
sudo systemctl start docker
```

Check:

```bash
sudo systemctl status docker --no-pager
```

Expected:

```text
Active: active (running)
```

---

# 4. Assign the Docker Group to the User

By default, Docker commands require `sudo`.

For example:

```bash
sudo docker ps
```

To allow the current user to execute Docker without `sudo`, add the user
to the `docker` group:

```bash
sudo usermod -aG docker $USER
```

Check the group:

```bash
getent group docker
```

The current user should appear in the output.

## 4.1 Restart WSL

Exit Ubuntu:

```bash
exit
```

From **PowerShell**:

```powershell
wsl --shutdown
```

Start Ubuntu again:

```powershell
wsl -d Ubuntu
```

Check groups:

```bash
groups
```

You should see:

```text
docker
```

---

# 5. Check Docker and Docker Compose

## 5.1 Check Docker version

```bash
docker version
```

You should see both:

```text
Client:
...

Server:
...
```

## 5.2 Check Docker daemon

```bash
systemctl status docker --no-pager
```

Expected:

```text
Active: active (running)
```

## 5.3 Run Docker test container

```bash
docker run hello-world
```

Expected output contains:

```text
Hello from Docker!
```

This confirms that:

* Docker CLI works
* Docker daemon works
* the current user can access Docker
* containers can be created and started

## 5.4 Check Docker Compose

```bash
docker compose version
```

Expected:

```text
Docker Compose version v2.x.x
```

## 5.5 Check Docker networks

```bash
docker network ls
```

A fresh Docker installation should normally contain:

```text
NETWORK ID     NAME      DRIVER    SCOPE
xxxxx         bridge    bridge    local
xxxxx         host      host      local
xxxxx         none      null      local
```

## 5.6 Test Docker Compose networking

Create a temporary test network:

```bash
docker network create test-network
```

Verify:

```bash
docker network ls
```

Then remove it:

```bash
docker network rm test-network
```

If this works, Docker can successfully create user-defined networks.

---

# Final Verification

At the end, the following commands should all work **without `sudo`**:

```bash
docker version
```

```bash
docker run hello-world
```

```bash
docker compose version
```

```bash
docker network ls
```

```bash
docker network create test-network
```

```bash
docker network rm test-network
```

The final architecture is:
```
Windows 11
|
+-- WSL2
|
+-- Ubuntu
|
+-- systemd
|
+-- Docker Engine
|    |
|    +-- Docker CLI
|    +-- containerd
|    +-- Buildx
|    +-- Docker Compose V2
|
+-- Docker user group
```


**Important:** This setup uses **native Docker Engine inside WSL2** and does
not require Docker Desktop.

