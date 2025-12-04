# Database Setup and Configuration

This application uses PostgreSQL database managed by EDB Postgres for Kubernetes Operator 1.27.1.

## Prerequisites

### EDB Operator Installation

Before deploying the application, ensure the EDB Postgres for Kubernetes Operator is installed in your cluster:

```bash
# Check if operator is installed
oc get ClusterServiceVersion/cloud-native-postgresql.v1.27.1

# Expected output:
# NAME                              DISPLAY                       VERSION   REPLACES   PHASE
# cloud-native-postgresql.v1.27.1   EDB Postgres for Kubernetes   1.27.1               Succeeded
```

If the operator is not installed, contact your platform team to install it cluster-wide.

### Storage Requirements

Ensure storage classes are available for PostgreSQL persistent volumes:

```bash
# List available storage classes
oc get storageclass

# The default storage class will be used automatically
```

## Automatic Database Provisioning

When you deploy this application, the following happens automatically:

1. **PostgreSQL Cluster Creation**: A PostgreSQL Cluster resource is created using EDB operator
2. **Database Instance**: A single PostgreSQL instance is provisioned
3. **Storage**: 1Gi persistent volume is allocated for database data
4. **Credentials**: Database username, password, and connection details are stored in a Kubernetes Secret
5. **Application Connection**: Environment variables are automatically injected into the application pod

## Database Configuration

### Resource Allocation

The database is optimized for demo purposes with minimal resources:

- **Storage**: 1Gi
- **CPU**: 50m request / 200m limit
- **Memory**: 128Mi request / 256Mi limit
- **Max Connections**: 20

### Database Connection

The application connects to the database using:

- **Host**: `<app-name>-postgresql-rw` (service name)
- **Port**: 5432
- **Database**: `<app-name>` (matches application name)
- **Username**: `<app-name>` (matches application name)
- **Password**: Stored in Secret `<app-name>-postgresql-credentials`

### Environment Variables

The following environment variables are automatically set in the application pod:

```bash
QUARKUS_DATASOURCE_DB_KIND=postgresql
QUARKUS_DATASOURCE_USERNAME=<from-secret>
QUARKUS_DATASOURCE_PASSWORD=<from-secret>
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://<app-name>-postgresql-rw:5432/<app-name>
```

## Database Migrations

### Flyway Integration

The application uses Flyway for database schema management:

- **Automatic Migrations**: Migrations run automatically on application startup
- **Migration Location**: `src/main/resources/db/migration/`
- **Initial Schema**: Created by `V1.0.0__create_names_table.sql`
- **Sample Data**: Initial names are inserted automatically

### Migration Files

Migration files follow the naming pattern: `V<version>__<description>.sql`

Example:
- `V1.0.0__create_names_table.sql` - Creates the names table and inserts initial data

## Managing the Database

### Viewing Database Status

```bash
# Check PostgreSQL cluster status
oc get cluster -n <namespace> <app-name>-postgresql

# Check PostgreSQL pods
oc get pods -n <namespace> | grep postgresql

# View cluster details
oc describe cluster -n <namespace> <app-name>-postgresql
```

### Accessing the Database

See the main [README.md](../../README.md) for detailed database management commands.

Quick reference:
```bash
# Set variables
export APP_NAME="your-app-name"
export NAMESPACE="your-namespace"
export DB_PASSWORD=$(oc get secret ${APP_NAME}-postgresql-credentials -n ${NAMESPACE} -o jsonpath='{.data.password}' | base64 -d)

# Connect to database
oc exec -it ${APP_NAME}-postgresql-1 -n ${NAMESPACE} -- env PGPASSWORD=${DB_PASSWORD} psql -h localhost -U ${APP_NAME} -d ${APP_NAME}
```

### Database Backup and Restore

For production environments, ensure you have a backup strategy:

1. **EDB Operator Backup**: Configure backup policies in the Cluster resource
2. **Manual Backup**: Use `pg_dump` via `oc exec`
3. **Point-in-Time Recovery**: Configure WAL archiving if needed

## Troubleshooting

### Database Not Starting

1. Check operator status:
   ```bash
   oc get ClusterServiceVersion/cloud-native-postgresql.v1.27.1
   ```

2. Check cluster events:
   ```bash
   oc describe cluster -n <namespace> <app-name>-postgresql
   ```

3. Check pod logs:
   ```bash
   oc logs -n <namespace> <app-name>-postgresql-1
   ```

### Connection Issues

1. Verify service exists:
   ```bash
   oc get svc -n <namespace> | grep postgresql
   ```

2. Test connection from application pod:
   ```bash
   oc exec -it <app-pod> -n <namespace> -- env | grep QUARKUS_DATASOURCE
   ```

3. Check network policies (if enabled)

### Storage Issues

1. Check persistent volume claims:
   ```bash
   oc get pvc -n <namespace> | grep postgresql
   ```

2. Verify storage class:
   ```bash
   oc get cluster -n <namespace> <app-name>-postgresql -o jsonpath='{.spec.storage.storageClass}'
   ```

## Production Considerations

For production deployments, consider:

1. **High Availability**: Configure multiple instances in the Cluster resource
2. **Backup Strategy**: Enable automated backups
3. **Resource Limits**: Adjust CPU and memory based on workload
4. **Storage**: Increase storage size and enable expansion
5. **Monitoring**: Set up database monitoring and alerting
6. **Security**: Review and harden database security settings
7. **Connection Pooling**: Tune connection pool settings in application

## Additional Resources

- [EDB Postgres for Kubernetes Documentation](https://www.enterprisedb.com/docs/postgres_for_kubernetes/latest/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Quarkus Database Guide](https://quarkus.io/guides/datasource)

