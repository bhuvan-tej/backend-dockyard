## 💡 Interview Questions

---
**Q: How does the Spring Boot app find PostgreSQL in Kubernetes?**
> Through Kubernetes DNS. The PostgreSQL Service is named
postgres-service. Kubernetes DNS resolves postgres-service
to the Service ClusterIP which load balances to the postgres Pod.
The Spring Boot app uses jdbc:postgresql://postgres-service:5432/appdb
in its datasource URL — identical to how it worked in Docker Compose
with container names.

**Q: Why use a PersistentVolumeClaim for PostgreSQL?**
> Without a PVC all database files live inside the Pod filesystem.
If the Pod restarts the data is gone. A PVC stores data on a
volume outside the Pod. New Pods mount the same PVC and find
all the data intact.

**Q: Why is replicas set to 1 for PostgreSQL?**
> Running multiple PostgreSQL replicas requires special setup —
primary and replica configuration, shared storage with
ReadWriteMany access mode. A single replica with a PVC is
sufficient for development. In production use a managed
database service like AWS RDS.

**Q: How do the Actuator probes work here vs the nginx demo?**
> In the nginx demo we used / as the probe path because nginx
has no Actuator. Here Spring Boot exposes dedicated endpoints.
/actuator/health/liveness returns DOWN only when Spring Boot
itself is broken. /actuator/health/readiness returns DOWN
when the database or redis is unavailable — triggering Pod
removal from the load balancer without a restart.