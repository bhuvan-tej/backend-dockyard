## 📋 Quick Reference

---
```powershell
# Log in to GHCR
echo $token | docker login ghcr.io --username $username --password-stdin
 
# Build with full registry tag
docker build -t ghcr.io/USERNAME/REPO/IMAGE:TAG .
 
# Add an extra tag to an existing image without rebuilding
docker tag SOURCE_IMAGE:TAG TARGET_IMAGE:TAG
 
# Push an image to the registry
docker push ghcr.io/USERNAME/REPO/IMAGE:TAG
 
# Pull an image from the registry
docker pull ghcr.io/USERNAME/REPO/IMAGE:TAG
 
# Log out from GHCR
docker logout ghcr.io
```