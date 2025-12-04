# Using Dev Containers with Cursor

This directory contains a `.devcontainer` configuration that allows you to use the same development environment locally in Cursor that is defined in the `.devfile.yaml` for Red Hat Dev Spaces.

## Two Options for Development

### Option 1: Use with Red Hat Dev Spaces (Recommended for OpenShift)

The `.devfile.yaml` in the root directory is designed for **Red Hat Dev Spaces** on OpenShift:

1. **Deploy your application** to Red Hat Dev Spaces
2. **Open the workspace** in Red Hat Dev Spaces web IDE
3. The devfile will automatically:
   - Start the Quarkus development container
   - Start the PostgreSQL sidecar container
   - Configure networking between containers
4. **Connect Cursor remotely** (optional):
   - Use SSH to connect to the Dev Spaces workspace
   - Or use the web-based IDE provided by Red Hat Dev Spaces

### Option 2: Use Locally with Cursor Dev Containers

The `.devcontainer` configuration allows you to run the same environment **locally** in Cursor:

#### Prerequisites

- **Docker Desktop** or **Docker Engine** installed and running
- **Cursor** with Dev Containers support (included by default)

#### Steps to Use

1. **Open the project in Cursor**:
   ```bash
   cursor /path/to/your/project
   ```

2. **Reopen in Container**:
   - Cursor should detect the `.devcontainer` folder
   - Click the notification to "Reopen in Container"
   - Or use Command Palette (`Cmd+Shift+P` / `Ctrl+Shift+P`)
   - Select: **"Dev Containers: Reopen in Container"**

3. **Wait for setup**:
   - Cursor will build/start the containers
   - This includes:
     - Development tooling container (Quarkus, Maven, Java)
     - PostgreSQL database container
   - First time may take a few minutes to download images

4. **Start developing**:
   - Once the container is ready, you're in the development environment
   - Run Quarkus in dev mode:
     ```bash
     ./mvnw compile quarkus:dev
     ```
   - The app will connect to PostgreSQL automatically
   - Access the app at `http://localhost:8080`

#### Container Details

- **Development Container**: `quay.io/devfile/universal-developer-image:ubi8-277c10c`
  - Contains: Java, Maven, Quarkus tooling
  - Ports: 8080 (Quarkus), 5005 (Debug)
  
- **PostgreSQL Container**: `postgres:15`
  - Database: `quarkusdb`
  - User: `quarkus` / Password: `quarkus`
  - Port: 5432
  - Data persists in Docker volume: `postgres-data`

#### Troubleshooting

- **Container won't start**: Ensure Docker is running
- **Port conflicts**: Stop other services using ports 8080, 5005, or 5432
- **Database connection issues**: Wait a few seconds after container start for PostgreSQL to initialize
- **Rebuild container**: Use Command Palette → "Dev Containers: Rebuild Container"

## Differences Between Devfile and Dev Container

| Feature | Devfile (`.devfile.yaml`) | Dev Container (`.devcontainer/`) |
|---------|---------------------------|----------------------------------|
| **Platform** | Red Hat Dev Spaces, Gitpod, Eclipse Che | VS Code, Cursor (local) |
| **Networking** | Containers in same pod (localhost) | Docker Compose network (service names) |
| **Deployment** | Cloud/OpenShift | Local Docker |
| **Use Case** | Production dev environments | Local development |

## Notes

- The devfile uses `localhost:5432` for PostgreSQL (same pod networking)
- The dev container uses `postgres:5432` (Docker Compose service name)
- Both configurations achieve the same result: Quarkus connects to PostgreSQL

