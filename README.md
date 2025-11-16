# SMS Checker / Frontend

Application service with frontend and backend for SMS checker
The frontend allows users to interact with the model in the backend through a web-based UI.

The frontend is implemented with Spring Boot and only consists of a website and one REST endpoint.
It **requires Java 25+** to run (tested with 25.0.1).
Any classification requests will be delegated to the `backend` service that serves the model.
You must specify the environment variable `MODEL_HOST` to define where the backend is running.

The frontend service can be started through running the `Main` class (e.g., in your IDE) or through Maven (recommended):

    MODEL_HOST="http://localhost:8081" mvn spring-boot:run

The server runs on port 8080. Once its startup has finished, you can access [localhost:8080/sms](http://localhost:8080/sms) in your browser to interact with the application.

## Docker Support (F3)

Build the Docker image:
```bash
docker build -t app:latest .
```

Run the container:
```bash
docker run -p 8080:8080 -e MODEL_HOST=http://localhost:8081 app:latest
```

Access the application at: http://localhost:8080/sms


## Multi-Architecture Support (F4)

This Docker image supports multiple CPU architectures, allowing it to run on different types of processors.

### Supported Architectures
- **linux/amd64** - Intel and AMD 64-bit processors (most servers and PCs)
- **linux/arm64** - ARM 64-bit processors (Apple Silicon Macs, AWS Graviton, Raspberry Pi)

### Implementation Details

We use **QEMU emulation** (Strategy 1 from Docker's multi-platform documentation):
- QEMU is built into Docker Desktop
- No Dockerfile changes required
- E.g. a Mac can emulate Intel processors to build AMD64 images and both architectures are built on a single machine

When users pull the image with `docker pull <image-name>`, Docker automatically downloads the correct version for their processor architecture.

### How to Build Multi-Architecture Images

**Prerequisites:**
- Docker Desktop with Buildx (included by default)

**Setup (first time only):**

Before building multi-architecture images, create a specialized builder:
```bash
docker buildx create --name multiarch-builder --use
docker buildx inspect --bootstrap
```

You only need to run these commands once on your machine. After this, the builder persists and you can use it anytime.

---

**For Development & Testing:**

When working locally and testing your changes, build for your computer's architecture:
```bash
# On Apple Silicon Macs (M1/M2/M3)
docker buildx build --platform linux/arm64 -t app:latest --load .

# On Intel/AMD computers
docker buildx build --platform linux/amd64 -t app:latest --load .
```

The `--load` flag makes the image available to run with `docker run` on your local machine. You can only load one architecture at a time because your computer can only run its native architecture efficiently.

**Then run it:**
```bash
docker run -p 8080:8080 -e MODEL_HOST=http://localhost:8081 app:latest
```

---

**For Production/Release:**

When releasing the application, build for both architectures:
```bash
docker buildx build --platform linux/amd64,linux/arm64 -t app:latest --push
```

This command:
- Builds for both Intel (amd64) and ARM (arm64) processors
- Creates a multi-platform image manifest
- Pushes both versions to a container registry (requires `--push` instead of `--load`)

Note: You cannot use `--load` with multiple platforms. Multi-architecture images must be pushed to a registry (like GitHub Container Registry or Docker Hub) to be useful.


### F4 Testing

Tested multi-architecture builds successfully:
- Built for both amd64 and arm64 simultaneously
- Verified AMD64 image runs on ARM64 Mac (with expected platform warning)
- Confirmed ARM64 native build works without warnings