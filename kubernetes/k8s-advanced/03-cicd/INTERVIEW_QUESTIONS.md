## 💡 Interview Questions

---
**Q: What is CI/CD and why is it important?**
> CI (Continuous Integration) automatically tests every code change.
CD (Continuous Deployment) automatically deploys passing builds.
Together they eliminate manual steps, catch bugs early, and allow
teams to ship multiple times per day with confidence.

**Q: Why run tests with real PostgreSQL and Redis in CI?**
> In-memory or mocked databases do not catch real integration bugs.
Using real service containers in CI matches the production setup
exactly — if it passes in CI it will work in production.

**Q: Why tag images with the git commit SHA?**
> SHA tags are immutable and unique per commit. They create a direct
link between what is running in production and the exact code that
built it. Unlike latest which changes silently, a SHA tag always
refers to the same image.

**Q: How does automatic rollback work?**
> The deploy job uses if: failure() to run the rollback step only
when a previous step failed. kubectl rollout undo restores the
previous Deployment revision which was already running and proven
healthy. The pipeline exits with an error so the team knows
to investigate even though production was restored.

**Q: What is the difference between CI and CD?**
> CI stops at building and testing — it produces a verified artifact
(the Docker image). CD takes that artifact and deploys it to an
environment. Some teams do CI/CD to staging automatically but
require manual approval before production.