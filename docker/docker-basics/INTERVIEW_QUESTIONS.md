## 📝 Interview Questions from Basics

---

**Q: What is Docker and what problem does it solve?**
> Docker packages an app with all its dependencies into a container
that runs identically everywhere. It solves the works on my machine
problem by eliminating differences between environments.

**Q: How does Docker differ from a Virtual Machine?**
> A VM includes a full OS and takes gigabytes and minutes to start.
A container shares the host OS kernel, takes megabytes and starts
in seconds. Containers are faster and more efficient.

**Q: What is the difference between an Image and a Container?**
> An image is a static read-only blueprint like a Java class.
A container is a running instance of that image like an object.
Many containers can run from one image at the same time.

**Q: How does Docker work on Windows?**
> Docker Desktop uses WSL2 to run a lightweight Linux kernel inside
Windows. Containers run as Linux processes inside WSL2.
Commands typed in PowerShell are routed transparently.

**Q: What does each Dockerfile instruction do?**
> FROM sets the base image. WORKDIR sets the working directory.
COPY copies files from host to image. RUN executes at build time.
ENV sets environment variables. EXPOSE documents the port.
CMD sets the default startup command.

**Q: What is the difference between RUN and CMD?**
> RUN executes at image build time for installing and compiling.
CMD executes when the container starts and can be overridden
at docker run time.

**Q: Why does layer order in a Dockerfile matter?**
> Docker caches each layer. A changed layer invalidates all layers
below it. Stable things like base image and installs go at the top.
Changing things like source code go at the bottom.

**Q: What is .dockerignore?**
> Like .gitignore for Docker. Excludes files from the build context.
Always exclude target, .idea and .git to speed up builds.

**Q: What happens to data when a container is removed?**
> All data written inside the container is permanently lost.
Use volumes to store data outside the container lifecycle.

**Q: What is the difference between a named volume and a bind mount?**
> Named volume is managed by Docker, best for production data.
Bind mount maps a specific Windows folder, best for development
where you want live file editing in IntelliJ.

**Q: How do containers communicate with each other?**
> Put them on the same Docker network. Docker provides built-in DNS
so each container is reachable by its container name.
No IP addresses needed.

**Q: How do you debug a crashing container?**
> Run docker logs to read the crash output. Run docker inspect to
check the exit code. 137 means OOMKilled so increase memory limit.
1 means application error so read the stack trace in the logs.

**Q: What does exit code 137 mean?**
> The container exceeded its memory limit and was forcibly killed
by the OS. Increase the container memory limit to fix it.

**Q: How do you copy a file out of a container?**
> docker cp containername:/path/inside /path/on/windows
Works on both running and stopped containers.
 
---