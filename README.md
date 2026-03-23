# TP CQRS avec Java Spring

Ce dépôt met en place une architecture CQRS conforme à ton schéma:

- `write-api` reçoit les commandes `POST`, `PUT`, `PATCH`
- `read-api` expose seulement les requêtes `GET`
- `write-postgres` stocke l'état d'écriture
- `sync-service` consomme Kafka et maintient la projection de lecture
- `read-postgres` expose la lecture à travers une vue SQL
- `kafka` tourne sur 3 brokers KRaft avec topic répliqué
- tout est déployable en pods K3s

## Structure

```text
common/          DTOs et événements partagés
write-api/       Spring Boot API commandes
read-api/        Spring Boot API lectures
sync-service/    Spring Boot consommateur Kafka -> projection SQL
k8s/             manifests K3s
scripts/         scripts PowerShell de build/test/deploiement
```

## Flux CQRS

1. Le client envoie une commande à `write-api`.
2. `write-api` écrit dans `write-postgres`.
3. `write-api` publie un événement `PRODUCT_CREATED` ou `PRODUCT_UPDATED` dans Kafka.
4. `sync-service` consomme l'événement.
5. `sync-service` met à jour `read-postgres.product_projection`.
6. `read-api` lit la vue `product_catalog_view`.

## Endpoints

### Write API

- `POST /products`
- `PUT /products/{id}`
- `PATCH /products/{id}`

Exemple:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name":"SSD NVMe 1TB",
    "description":"Produit cree depuis le TP CQRS",
    "price":89.99,
    "stock":12,
    "status":"AVAILABLE"
  }'
```

### Read API

- `GET /products`
- `GET /products/{id}`

Exemple:

```bash
curl http://localhost:8081/products
```

## Build de l'image

Java n'est pas requis localement si tu construis avec Docker:

```powershell
docker build -t cqrs-spring:latest .
```

L'image contient les trois jars Spring Boot. Le conteneur choisi l'application via la variable `APP_NAME`.

## Déploiement K3s

Le dépôt contient un script PowerShell prêt à l'emploi:

```powershell
.\scripts\deploy-to-k3s.ps1
```

Ce script:

1. build l'image Docker `cqrs-spring:latest`
2. exporte l'image en tar
3. copie l'image et les manifests sur `nodemaster`
4. importe l'image dans `k3s ctr`
5. applique `kubectl apply -k k8s`

Les trois services Spring sont volontairement contraints sur `nodemaster` pour éviter d'importer l'image custom sur tous les nœuds. Les images PostgreSQL et Kafka, elles, sont téléchargées directement par le cluster.

## Vérification

Depuis la machine `nodemaster`:

```bash
sudo k3s kubectl get pods -n cqrs-tp -o wide
sudo k3s kubectl get pvc -n cqrs-tp
```

Port-forward des APIs:

```bash
sudo k3s kubectl port-forward -n cqrs-tp svc/write-api 8080:8080
sudo k3s kubectl port-forward -n cqrs-tp svc/read-api 8081:8081
```

Puis, depuis Windows/PowerShell:

```powershell
.\scripts\test-cqrs.ps1
```

## Points importants pour le TP

- la lecture et l'écriture sont séparées
- la base de lecture est alimentée de façon asynchrone
- la vue SQL du read side illustre la projection CQRS
- Kafka absorbe le flux d'événements et découple les deux bases
- la lecture reste indépendante de la latence de l'écriture
