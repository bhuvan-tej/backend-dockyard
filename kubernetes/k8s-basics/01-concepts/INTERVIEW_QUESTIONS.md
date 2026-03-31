## 💡 Interview Questions

---
**Q: What is Kubernetes and what problem does it solve?**
> Kubernetes orchestrates containers across a cluster of machines.
It handles scheduling, self-healing, scaling, rolling updates and
configuration management — problems Docker alone cannot solve
at production scale.

**Q: What is the role of the API Server?**
> It is the single entry point for all cluster operations. Every
kubectl command, every internal component and every tool
communicates through the API Server which validates requests
and persists state to etcd.

**Q: What is etcd?**
> A distributed key-value database that stores all cluster state.
What Deployments exist, how many replicas are desired, what Pods
are running. Losing etcd means losing the cluster — always backed up.

**Q: What is the difference between kubelet and kube-proxy?**
> kubelet is the node agent that manages containers on that node.
kube-proxy manages network rules to implement Service routing —
directing traffic addressed to a Service to the correct Pod.

**Q: What is a Namespace?**
> A way to divide a single cluster into logical isolated sections.
Teams use separate namespaces for dev, staging and production
running in the same cluster without interfering with each other.