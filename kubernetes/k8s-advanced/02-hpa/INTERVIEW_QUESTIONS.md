## 💡 Interview Questions

**Q: What is a Horizontal Pod Autoscaler?**
> HPA automatically scales the number of Pod replicas in a Deployment
based on observed metrics like CPU or memory usage. It reads metrics
from the Metrics Server every 15 seconds and adds or removes Pods
to keep utilisation near the target threshold.

**Q: What is required for HPA to work?**
> Two things. First the Metrics Server must be running in the cluster
to collect Pod resource usage. Second the target Deployment must
have CPU resource requests set — HPA calculates percentage usage
relative to the requested amount.

**Q: Why is scale-down slower than scale-up?**
> Scale-down uses a longer stabilisation window to avoid removing
Pods too quickly when traffic is temporarily low. If you remove
Pods and traffic spikes immediately after you are under-scaled
and users experience slowness while HPA scales back up.

**Q: What is the difference between HPA and VPA?**
> HPA scales horizontally — adds or removes Pod replicas.
VPA (Vertical Pod Autoscaler) scales vertically — adjusts CPU
and memory requests on individual Pods. HPA is for stateless
apps that can run multiple instances. VPA is for stateful apps
or when horizontal scaling is not possible.

**Q: What happens if HPA and a manual kubectl scale conflict?**
> HPA takes control. If you manually scale to 1 replica but
minReplicas is 2 HPA will immediately scale back up to 2.
HPA always wins when it is enabled on a Deployment.