param(
    [string]$ImageName = "cqrs-spring:latest",
    [string]$RemoteHost = "nodemaster",
    [string]$RemoteWorkdir = "~/cqrs-pattern"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$archivePath = Join-Path $repoRoot "cqrs-spring.tar"
$wslRepoRoot = (wsl wslpath -a $repoRoot).Trim()
$wslArchivePath = (wsl wslpath -a $archivePath).Trim()

Write-Host "1. Build Docker image $ImageName"
docker build -t $ImageName $repoRoot

Write-Host "2. Export Docker image"
docker save $ImageName -o $archivePath

Write-Host "3. Prepare remote workspace on $RemoteHost"
wsl ssh $RemoteHost "rm -rf $RemoteWorkdir && mkdir -p $RemoteWorkdir"

Write-Host "4. Copy image and manifests"
wsl scp $wslArchivePath "${RemoteHost}:/tmp/cqrs-spring.tar"
wsl scp -r "${wslRepoRoot}/k8s" "${RemoteHost}:${RemoteWorkdir}/"

Write-Host "5. Import image and apply manifests"
wsl ssh -t $RemoteHost "sudo k3s ctr images import /tmp/cqrs-spring.tar && rm -f /tmp/cqrs-spring.tar && sudo k3s kubectl apply -k ${RemoteWorkdir}/k8s"

Write-Host "6. Show pods"
wsl ssh -t $RemoteHost "sudo k3s kubectl get pods -n cqrs-tp -o wide"

Remove-Item $archivePath -ErrorAction SilentlyContinue

