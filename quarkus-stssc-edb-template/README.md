# Quarkus Web Template with PostgreSQL (EDB Operator)

This repository contains the Backstage Template used to create the Kubernetes resources needed to build/deploy a Quarkus application with a PostgreSQL database using EDB Kubernetes Operator 1.27.1.

## Prerequisit

Installed EDB Kubernetes Operator >=1.27.1

## Features

- Quarkus application deployment with Tekton CI/CD pipeline
- PostgreSQL database deployment using EDB Kubernetes Operator 1.27.1
- Database and application deployed in the same namespace
- Automatic database credentials management via Kubernetes Secrets
- Database connection environment variables automatically configured in the application deployment

## Repository Breakdown

- `template.yaml`: Main template definition for Red Hat Developer Hub
- `skeleton/`: Application source code template
- `gitops-template/`: GitOps manifests including:
  - Application deployment (Deployment, Service, Route)
  - PostgreSQL Cluster resource (EDB Operator)
  - PostgreSQL credentials Secret
  - ArgoCD application definitions

## PostgreSQL Configuration

The template creates a PostgreSQL cluster optimized for demo purposes with minimal resources:
- 1 instance
- 1Gi storage (sufficient for demo data)
- CPU: 50m request, 200m limit
- Memory: 128Mi request, 256Mi limit
- Optimized PostgreSQL parameters for minimal footprint:
  - Max connections: 20 (sufficient for demo)
  - Shared buffers: 64MB
  - Effective cache: 256MB
  - Minimal WAL sizes
- Database name and owner matching the application name
- Credentials stored in a Kubernetes Secret
- Connection details automatically injected into the application via environment variables

## Resource Optimization

This template is optimized for demo purposes with minimal resource usage:

**PostgreSQL Database:**
- Storage: 1Gi
- CPU: 50m request / 200m limit
- Memory: 128Mi request / 256Mi limit

**Quarkus Application:**
- CPU: 10m request / 500m limit
- Memory: 128Mi request / 768Mi limit
- JVM Heap: ~537Mi (70% of container memory)
- Connection pool: 1-5 connections (minimal footprint)

**Total Resource Usage (per application):**
- CPU: ~60m request / ~700m limit
- Memory: ~256Mi request / ~1024Mi limit
- Storage: 1Gi

These settings provide smooth operation for demo purposes while minimizing resource consumption.

## Prerequisites

- EDB Kubernetes Operator 1.27.1 must be installed in the cluster
- Operator should be available in the target namespace or cluster-wide

## Application Features

The Quarkus application includes a `/hello` endpoint that:
- Reads names from a PostgreSQL `names` table
- Returns "Hello [name]" where the name is selected in round-robin fashion
- Each application reload cycles through the names sequentially

## Managing the Names Table

The application uses a PostgreSQL database with a `names` table. All commands below can be **copy-pasted directly** - just set your application name once at the beginning.

### Initial Setup (Run Once)

**Set your application name** (the component name you chose when creating the template):

```bash
export APP_NAME="my-edb-quarkus-3"
export NAMESPACE="tssc-app-development"
```

**Or for stage/production:**
```bash
export APP_NAME="my-edb-quarkus-3"
export NAMESPACE="tssc-app-stage"  # or tssc-app-prod
```

**Get the database password** (automatically retrieved and stored):
```bash
export DB_PASSWORD=$(oc get secret ${APP_NAME}-postgresql-credentials -n ${NAMESPACE} -o jsonpath='{.data.password}' | base64 -d)
```

### Viewing All Names

```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "SELECT * FROM names;"
```

### Adding a New Name

**Add a single name:**
```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "INSERT INTO names (name) VALUES ('Frank');"
```

**Add multiple names at once:**
```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "INSERT INTO names (name) VALUES ('Grace'), ('Henry'), ('Iris');"
```

### Deleting a Name

**Delete by name:**
```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "DELETE FROM names WHERE name = 'NameToDelete';"
```

**Delete all names:**
```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "DELETE FROM names;"
```

### Updating a Name

```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "UPDATE names SET name = 'NewName' WHERE id = 1;"
```

### Interactive Database Session

For multiple operations, start an interactive session:

```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME}
```

Once in the interactive session, run SQL commands directly:
```sql
SELECT * FROM names;
INSERT INTO names (name) VALUES ('NewName');
UPDATE names SET name = 'Updated' WHERE id = 1;
DELETE FROM names WHERE name = 'OldName';
\q  -- Exit the session
```

### Testing the Application

After modifying the database, test the `/hello` endpoint:

```bash
# Get the route URL and test
ROUTE_URL=$(oc get route ${APP_NAME} -n ${NAMESPACE} -o jsonpath='{.spec.host}')
curl http://${ROUTE_URL}/hello
```

Or access directly:
```bash
curl http://${APP_NAME}-${NAMESPACE}.apps.cluster-cvphk.dynamic.redhatworkshops.io/hello
```

The endpoint cycles through the names in the database, so each request may return a different name.

### Quick Helper Function (Optional)

Add this to your `~/.bashrc` or `~/.zshrc` for even easier access:

```bash
# PostgreSQL database helper function
db-exec() {
  local sql="$1"
  if [ -z "$DB_PASSWORD" ]; then
    export DB_PASSWORD=$(oc get secret ${APP_NAME}-postgresql-credentials -n ${NAMESPACE} -o jsonpath='{.data.password}' | base64 -d)
  fi
  oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "$sql"
}

# Usage examples:
# db-exec "SELECT * FROM names;"
# db-exec "INSERT INTO names (name) VALUES ('NewName');"
# db-exec "DELETE FROM names WHERE name = 'OldName';"
```

After adding the function, reload your shell:
```bash
source ~/.bashrc  # or source ~/.zshrc
```

Then use it like:
```bash
db-exec "SELECT * FROM names;"
db-exec "INSERT INTO names (name) VALUES ('Test');"
```

### Troubleshooting

**If the pod name doesn't match the pattern, find it automatically:**
```bash
PG_POD=$(oc get pods -n ${NAMESPACE} -l app.kubernetes.io/name=${APP_NAME}-postgresql -o jsonpath='{.items[0].metadata.name}')
oc exec -it ${PG_POD} -n ${NAMESPACE} -- psql -U ${APP_NAME} -d ${APP_NAME} -c "SELECT * FROM names;"
```

**Test database connection:**
```bash
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME} -c "SELECT version();"
```
