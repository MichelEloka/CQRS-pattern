param(
    [string]$WriteApiBaseUrl = "http://localhost:8080",
    [string]$ReadApiBaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Stop"

$payload = @{
    name = "SSD NVMe 1TB"
    description = "Produit cree depuis le TP CQRS"
    price = 89.99
    stock = 12
    status = "AVAILABLE"
} | ConvertTo-Json

Write-Host "POST $WriteApiBaseUrl/products"
$created = Invoke-RestMethod -Method Post -Uri "$WriteApiBaseUrl/products" -ContentType "application/json" -Body $payload
$created | ConvertTo-Json -Depth 5

Write-Host "Pause de 5 secondes pour laisser Kafka + sync-service propager la vue de lecture"
Start-Sleep -Seconds 5

Write-Host "GET $ReadApiBaseUrl/products/$($created.id)"
$projection = Invoke-RestMethod -Method Get -Uri "$ReadApiBaseUrl/products/$($created.id)"
$projection | ConvertTo-Json -Depth 5

