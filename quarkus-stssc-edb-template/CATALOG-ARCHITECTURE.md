# Backstage Catalog Architecture for Quarkus EDB Template

This document describes the Backstage catalog structure for applications created using the `quarkus-stssc-edb-template`. The catalog entries provide a clear architectural overview of how the Quarkus application interacts with the PostgreSQL database managed by the EDB Operator.

## Catalog Structure

When an application is created from this template, the following Backstage catalog entities are generated:

### 1. System
- **Name**: `{app-name}-system`
- **Type**: `System`
- **Purpose**: Groups all related components and resources together
- **Tags**: `quarkus`, `postgresql`, `edb`
- **Owner**: Set from template input

### 2. Database Resource
- **Name**: `{app-name}-postgresql`
- **Type**: `Resource` (type: `database`)
- **Purpose**: Represents the PostgreSQL database cluster managed by EDB Operator
- **Annotations**:
  - `backstage.io/kubernetes-id`: Links to Kubernetes resource
  - `postgresql.k8s.enterprisedb.io/cluster`: Cluster name
  - `postgresql.k8s.enterprisedb.io/operator-version`: "1.27.1"
- **Tags**: `postgresql`, `database`, `edb`, `managed-service`
- **System**: `{app-name}-system`
- **Lifecycle**: `production`

### 3. Application Component
- **Name**: `{app-name}`
- **Type**: `Component` (type: `service`)
- **Purpose**: Represents the Quarkus application service
- **Dependencies**:
  - `dependsOn`: `resource:{app-name}-postgresql`
- **System**: `{app-name}-system`
- **Tags**: `java`, `quarkus`, `postgresql`
- **Provides APIs**: OpenAPI specification

### 4. GitOps Resource
- **Name**: `{app-name}-gitops`
- **Type**: `Resource` (type: `gitops`)
- **Purpose**: Represents the GitOps repository and ArgoCD application
- **Dependencies**:
  - `dependsOn`: 
    - `component:{app-name}`
    - `resource:{app-name}-postgresql`
- **System**: `{app-name}-system`

### 5. API
- **Name**: `{app-name}`
- **Type**: `API` (type: `openapi`)
- **Purpose**: OpenAPI specification for the application

## Architecture Diagram

The catalog structure creates the following dependency graph:

```
System: {app-name}-system
├── Resource: {app-name}-postgresql (database)
│   └── Managed by: EDB Postgres for Kubernetes Operator 1.27.1
│   └── Kubernetes Resource: Cluster (postgresql.k8s.enterprisedb.io/v1)
│
├── Component: {app-name} (service)
│   └── dependsOn: resource:{app-name}-postgresql
│   └── Provides: API {app-name}
│
├── Resource: {app-name}-gitops (gitops)
│   └── dependsOn: 
│       ├── component:{app-name}
│       └── resource:{app-name}-postgresql
│
└── API: {app-name} (openapi)
    └── Provided by: component:{app-name}
```

## OpenShift Deployment Architecture

On OpenShift, the actual deployment structure is:

```
Namespace: {namespace}
├── EDB Operator (Cluster-wide, managed by platform team)
│   └── ClusterServiceVersion: cloud-native-postgresql.v1.27.1
│
├── PostgreSQL Cluster (Managed by EDB Operator)
│   ├── CustomResource: Cluster (postgresql.k8s.enterprisedb.io/v1)
│   │   └── Name: {app-name}-postgresql
│   ├── StatefulSet: {app-name}-postgresql-{instance}
│   ├── Service: {app-name}-postgresql-rw (read-write)
│   ├── Service: {app-name}-postgresql-ro (read-only)
│   ├── Service: {app-name}-postgresql-r (read)
│   └── Secret: {app-name}-postgresql-credentials
│
└── Quarkus Application
    ├── Deployment: {app-name}
    ├── Service: {app-name}
    └── Route: {app-name}
    └── Environment Variables:
        ├── QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://{app-name}-postgresql-rw:5432/{app-name}
        ├── QUARKUS_DATASOURCE_USERNAME: (from secret)
        └── QUARKUS_DATASOURCE_PASSWORD: (from secret)
```

## Key Relationships

1. **Application → Database**: The Quarkus application depends on the PostgreSQL database resource
2. **Database → EDB Operator**: The PostgreSQL cluster is managed by the EDB Postgres for Kubernetes Operator (cluster-wide infrastructure)
3. **GitOps → Application + Database**: The GitOps resource manages both the application and database deployments

## Benefits for Architects

This catalog structure provides:

1. **Clear Dependency Visualization**: Architects can see at a glance that the application depends on PostgreSQL
2. **Infrastructure Awareness**: The database resource annotations indicate it's managed by EDB Operator 1.27.1
3. **System-Level View**: All related components are grouped under a single system
4. **Kubernetes Integration**: Annotations link Backstage entities to actual Kubernetes resources
5. **Lifecycle Management**: Resources are tagged with lifecycle stages (production, experimental)

## Viewing in Backstage

In Red Hat Developer Hub, architects can:

1. Navigate to the System page to see the complete architecture
2. View the Component page to see database dependencies
3. Inspect the Resource page to see database configuration and operator details
4. Use the dependency graph to understand data flow and relationships
5. Access Kubernetes resources directly through annotations

