# ${{values.name}} Documentation

${{values.description}}

## Overview

This Quarkus application includes PostgreSQL database support using EDB (EnterpriseDB) Postgres for Kubernetes Operator. The application automatically provisions and connects to a PostgreSQL database instance in the same namespace.

## Key Features

- **Quarkus Framework**: Modern Java framework optimized for cloud-native applications
- **PostgreSQL Database**: Managed by EDB Kubernetes Operator 1.27.1
- **Automatic Database Provisioning**: Database is created automatically when the application is deployed
- **Database Migrations**: Flyway handles database schema migrations automatically
- **Health Checks**: Built-in health endpoints for Kubernetes liveness and readiness probes

## Dependencies

### Required Operators

- **EDB Postgres for Kubernetes Operator 1.27.1**: Must be installed in the cluster
  - Display Name: EDB Postgres for Kubernetes
  - CSV: cloud-native-postgresql.v1.27.1
  - Verify installation: `oc get ClusterServiceVersion/cloud-native-postgresql.v1.27.1`

### Application Dependencies

- PostgreSQL database (automatically provisioned)
- Quarkus runtime dependencies (included in container image)

## Quick Start

1. Ensure EDB Postgres for Kubernetes Operator 1.27.1 is installed
2. Deploy the application using the provided GitOps manifests
3. The PostgreSQL database will be automatically created in the same namespace
4. Database migrations run automatically on application startup

## Documentation

- [Architecture](architecture.md) - System architecture and component details
- [Onboarding to OpenShift](onboard-openshift.md) - Guide for developers and platform engineers
- [Database Setup](database-setup.md) - PostgreSQL database configuration and management
