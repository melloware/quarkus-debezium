param (
    [Parameter(Mandatory = $true)]
    [string]$JsonFilePath
)

$headers = @{
    "Content-Type" = "application/json"
}

# Read JSON from the specified file
if (-Not (Test-Path $JsonFilePath)) {
    Write-Host "Error: File '$JsonFilePath' not found." -ForegroundColor Red
    exit 1
}

$body = Get-Content -Path $JsonFilePath -Raw | ConvertFrom-Json | ConvertTo-Json -Depth 10

$url = "http://localhost:8083/connectors"

Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body
