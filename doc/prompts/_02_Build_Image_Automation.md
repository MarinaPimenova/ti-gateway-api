
I want to automate the builds and images creation in the repositories.
So, please look at the scripts in the `k8s`:
```text
[build-all-docker-images.sh](../../k8s/build-all-docker-images.sh)
[build-all-target-and-image.sh](../../k8s/build-all-target-and-image.sh)
[build-all-targets.sh](../../k8s/build-all-targets.sh)
[build-docker-image.sh](../../k8s/build-docker-image.sh)
[build-target.sh](../../k8s/build-target.sh)
```

and align them according to
- doc/_06_LOCAL_SETUP.md.
- docker/docker-compose-full.yml.

As a result, the following scripts should be refactored:
- build-all-docker-images.sh.
- build-all-targets.sh.
- build-all-target-and-image.sh.
Please provide complete guide how the execution sequence of the above bash scripts.
