# Pushing Images to GitHub Container Registry

## 🎯 Goal

---
Build a Docker image, tag it correctly and push it to GitHub Container Registry (GHCR).
This is how your images get from your machine to a server, a CI/CD pipeline or a Kubernetes cluster.

## What Is a Registry?

---
```
A registry is remote storage for Docker images.
Like Maven Central stores JARs, a registry stores Docker images.
 
When you run:
  docker run nginx
 
Docker checks if the nginx image exists locally.
If not it pulls it from Docker Hub which is the default public registry.
 
You can push your own images to:
  Docker Hub                  hub.docker.com           public, free tier available
  GitHub Container Registry   ghcr.io                  free for public repos
  AWS ECR                                              private, paid
  Google Artifact Registry                             private, paid
 
We use GHCR because your code is already on GitHub and it is free for public repositories.
```

## How Image Tagging Works

---
```
A full image tag looks like this:
 
  ghcr.io / YOUR_USERNAME / backend-dockyard / hello-docker : v1.0
  ───────   ─────────────   ─────────────────   ────────────  ────
  registry  your GitHub      repository name     image name   version
            username
 
Examples:
  ghcr.io/johndoe/backend-dockyard/hello-docker:v1.0
  ghcr.io/johndoe/backend-dockyard/hello-docker:latest
  ghcr.io/johndoe/backend-dockyard/hello-docker:sha-a1b2c3d
```

## Step 1 — Create a GitHub Personal Access Token

---
GHCR needs a token to authenticate you when pushing images.

```
1. Go to https://github.com/settings/tokens
2. Click Generate new token → Generate new token (classic)
3. Give it a name like  docker-push
4. Set expiration to 90 days
5. Under Select scopes tick:
     write:packages    allows pushing images to GHCR
     read:packages     allows pulling images from GHCR
     delete:packages   allows deleting images if needed
6. Click Generate token
7. COPY THE TOKEN NOW — GitHub only shows it once
```

## Step 2 — Log In to GHCR From the Terminal

---
```powershell
# Store your token in a variable so you do not have to type it repeatedly
# Replace YOUR_TOKEN with the token you just copied from GitHub
$token = "YOUR_TOKEN"
 
# Replace YOUR_USERNAME with your actual GitHub username
$username = "YOUR_USERNAME"
 
# Log in to GitHub Container Registry
# --username is your GitHub username
# --password-stdin reads the password from the pipeline instead of
#   typing it directly which would expose it in your terminal history
echo $token | docker login ghcr.io --username $username --password-stdin
 
# You should see: Login Succeeded
```

## Step 3 — Build the Image With the Correct Tag

---
```powershell
# Navigate to the dockerfile folder which has HelloDocker.java and Dockerfile
cd docker\docker-basics\02-dockerfile
 
# Build the image with a full GHCR tag
# Replace YOUR_USERNAME with your actual GitHub username
# The tag format ghcr.io/username/repo/imagename:version tells Docker
# exactly where to push this image when you run docker push
docker build -t ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:v1.0 .
 
# Verify the image was built and tagged correctly
# Look for your image in the list with the full ghcr.io tag
docker images | Select-String "hello-docker"
```

## Step 4 — Push the Image to GHCR

```powershell
# Push the image to GitHub Container Registry
# Docker reads the tag to know which registry and repository to push to
# ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:v1.0
docker push ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:v1.0
 
# Also tag it as latest and push that too
# latest is a convention meaning the most recent stable version
# -t adds an additional tag to an existing image without rebuilding
docker tag ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:v1.0 `
           ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:latest
 
# Push the latest tag
docker push ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:latest
```

## Step 5 — Verify on GitHub

---
```
1. Go to https://github.com/YOUR_USERNAME/backend-dockyard
2. Click the Packages tab on the right side of the page
3. You should see hello-docker listed as a package
4. Click it to see the image details, tags and pull command
```

## Step 6 — Pull the Image From GHCR

---
```powershell
# Delete the local image first so we prove the pull works
# This removes both tags we pushed
docker rmi ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:v1.0
docker rmi ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:latest
 
# Pull the image back from GHCR
# This is exactly what a server or Kubernetes would do to get your image
docker pull ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:latest
 
# Run it to confirm it works after being pulled from the registry
docker run --rm ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:latest
```

## Step 7 — Make the Package Public (Optional)

---
By default, GHCR packages are private. To make your image publicly
pullable without authentication:

```
1. Go to https://github.com/YOUR_USERNAME/backend-dockyard/packages
2. Click hello-docker
3. Click Package settings on the right
4. Scroll down to Danger Zone
5. Click Change visibility → Public
```

After this anyone can pull your image with:
```powershell
# No login needed for public packages
docker pull ghcr.io/YOUR_USERNAME/backend-dockyard/hello-docker:latest
```

## How This Connects to CI/CD

---
In the Kubernetes section you will build a GitHub Actions pipeline that:

```
1. You push code to GitHub
2. GitHub Actions automatically:
     runs tests
     builds the Docker image
     tags it with the git commit SHA  e.g. sha-a1b2c3d
     pushes it to GHCR
     deploys it to Kubernetes
3. Your app is live with zero manual steps
```

The image tag with the commit SHA means you can always trace exactly
which version of your code is running in production.

## 📝 Interview Questions

---
**Q: What is a container registry?**
> Remote storage for Docker images. Like Maven Central for JARs.
You push images to a registry after building them and pull
them on servers or in CI/CD pipelines to run them.

**Q: What is the difference between Docker Hub and GHCR?**
> Docker Hub is the default public registry. GHCR is GitHub's
registry, free for public repos and tightly integrated with
GitHub Actions. Most teams use GHCR if their code is on GitHub
because authentication uses existing GitHub tokens.

**Q: Why tag images with the git commit SHA?**
> It creates a direct link between a running container and the
exact code that built it. If something breaks in production
you can immediately see which commit caused it and roll back
to a specific previous version.

**Q: What is the latest tag and should you use it in production?**
> latest is a convention for the most recent build. In production
you should use specific version tags like v1.2.0 or commit SHAs
instead of latest because latest changes silently and makes
deployments unpredictable.