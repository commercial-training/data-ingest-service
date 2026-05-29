# data-ingest-service

Spring Boot Event Hubs consumer that writes report records to Cosmos DB.

## Local build

```powershell
mvn -B -ntp verify
docker build -t data-ingest-service:local .
```

## GitHub remote

```powershell
git add .
git commit -m "feat: initialize data ingest service repository"
git remote add origin <data-ingest-service-repo-url>
git push -u origin main
```

## GitHub configuration

Required repository secrets:

- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_SUBSCRIPTION_ID`

Required repository variables:

- `ACR_NAME`
- `AZURE_RESOURCE_GROUP`
