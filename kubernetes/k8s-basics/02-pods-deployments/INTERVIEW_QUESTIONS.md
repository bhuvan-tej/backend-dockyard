## 💡 Interview Questions

---
**Q: What is the difference between a Pod and a Deployment?**
> A Pod is a single container instance with no auto-recovery.
If it dies it stays dead. A Deployment declares desired state
and Kubernetes continuously reconciles actual state to match it,
recreating Pods automatically when they crash.

**Q: What is a ReplicaSet?**
> The object a Deployment creates internally to maintain the desired
Pod count. Deployment creates ReplicaSet which creates Pods.
You rarely interact with ReplicaSets directly.

**Q: What happens during a rolling update?**
> Kubernetes creates new Pods with the updated image while keeping
old Pods running. Once new Pods are healthy old Pods are terminated.
maxUnavailable controls how many old Pods can be down at once.
maxSurge controls how many extra Pods can exist during the update.

**Q: How does Kubernetes self-healing work?**
> The Controller Manager continuously watches the cluster.
When actual state differs from desired state it takes action.
If a Pod crashes the Controller Manager creates a replacement.
If a node fails Pods are rescheduled to healthy nodes.

**Q: What is terminationGracePeriodSeconds?**
> How long Kubernetes waits after sending SIGTERM before force
killing with SIGKILL. During this window the app should finish
handling in-flight requests and shut down cleanly.
Default is 30 seconds.