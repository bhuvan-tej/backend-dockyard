## 💡 Interview Questions

---
**Q: What are the three Kubernetes probes and what does each do?**
> startupProbe gives a slow-starting app time to initialise before
other probes begin. livenessProbe checks if the app is alive —
failure causes a container restart. readinessProbe checks if the
app is ready for traffic — failure removes the Pod from the load
balancer without restarting it.

**Q: What is the difference between liveness and readiness failure?**
> Liveness failure means the app is broken and needs a restart.
Readiness failure means the app is alive but temporarily cannot
serve requests — removed from load balancer until it recovers.
Readiness never causes a restart.

**Q: When should you use readiness instead of liveness?**
> When restarting the container would not fix the problem. If the
database goes down restarting the app does nothing. Readiness
removes the Pod from traffic until the database recovers.

**Q: What is CrashLoopBackOff?**
> A Pod state where the container keeps crashing and Kubernetes
keeps restarting it with increasing delays between attempts.
Common causes: liveness probe failing, app crashing on startup,
wrong probe path configured.

**Q: Why is startupProbe important for Spring Boot?**
> Spring Boot takes 30-60 seconds to start. Without startupProbe
the livenessProbe would fail during startup because endpoints
are not ready yet. Kubernetes would restart the Pod repeatedly
in a loop — CrashLoopBackOff. startupProbe blocks liveness
and readiness until startup completes.