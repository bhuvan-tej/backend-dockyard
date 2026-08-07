## 💡 Interview Questions

**Q: What is an Ingress in Kubernetes?**
> Ingress is a configuration object that defines HTTP/HTTPS routing
rules for reaching Services inside the cluster. It gives you a single
entry point that routes traffic to multiple backend Services based on
the request path or hostname. Ingress itself does not move traffic —
it is just a set of rules.

**Q: What is the difference between an Ingress and an Ingress Controller?**
> The Ingress is the rule set (the YAML you write). The Ingress
Controller is the actual proxy — usually nginx — running inside the
cluster that reads those rules and does the real routing. Without a
controller installed, an Ingress object does nothing. In minikube you
enable it with `minikube addons enable ingress`.

**Q: When would you use Ingress over NodePort or LoadBalancer?**
> NodePort opens a port on every node and is only good for local dev.
LoadBalancer creates one external load balancer (and often one cloud
IP) per Service, which gets expensive with many services. Ingress
gives you one entry point that routes to many services by path or
host, so it is the standard choice for exposing multiple services in
production.

**Q: What is the difference between path-based and host-based routing?**
> Path-based routing uses the same hostname with different paths —
`localhost/products` and `localhost/orders` go to different Services.
Host-based routing uses different hostnames — `api.myapp.com` and
`admin.myapp.com` go to different Services. Both can be combined in a
single Ingress.

**Q: Why is the rewrite-target annotation needed for path-based routing?**
> The backend Service usually serves its content at `/`, not at
`/products`. Without a rewrite, a request for `/products` is forwarded
as `/products` and the app returns 404. `rewrite-target: /$1` strips
the matched prefix so the backend receives `/`, and it responds
correctly.

**Q: What is the full traffic flow for an Ingress request?**
> Client → Ingress Controller → Service → Pod. Ingress never talks to
Pods directly — a Service is always required in the path because the
Service provides the stable DNS name and load balancing across Pods.

