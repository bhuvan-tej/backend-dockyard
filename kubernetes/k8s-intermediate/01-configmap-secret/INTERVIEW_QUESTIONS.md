## 💡 Interview Questions

---
**Q: What is the difference between a ConfigMap and a Secret?**
> ConfigMap stores non-sensitive configuration like URLs, ports and
feature flags as plain text. Secret stores sensitive data like
passwords and tokens as base64 encoded values. Neither is
encrypted by default — Secrets need additional setup for
encryption at rest in production.

**Q: Is base64 encoding in Secrets secure?**
> No. Base64 is encoding not encryption. Anyone with access to
the cluster can decode Secret values. In production use tools
like Sealed Secrets, AWS Secrets Manager or HashiCorp Vault
to properly protect sensitive data.

**Q: What is the difference between envFrom and env valueFrom?**
> envFrom loads all keys from a ConfigMap or Secret as env vars
in one line. env valueFrom loads specific keys one by one and
lets you rename them. Use envFrom for convenience, env valueFrom
when you need specific keys or different variable names.

**Q: Do Pods automatically pick up ConfigMap changes?**
> Not immediately. Pods read ConfigMap values at startup. To pick
up changes you need to restart the Pods using
kubectl rollout restart deployment. Volume mounted ConfigMaps
update automatically but env var injected ones do not.