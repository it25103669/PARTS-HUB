$uri = "http://localhost:8080/api/sales"
$headers = @{
    "Content-Type" = "application/json"
    "X-User-Role"  = "Business Owner"
}
$body = @{
    invoiceNumber = "INV-TEST-001"
    customerName = "Test Customer"
    customerContact = "0770000000"
    saleDate = "2026-09-01"
    paymentMethod = "Cash"
    paymentStatus = "PAID"
    items = @(
        @{
            partName = "ASUS ROG Strix RTX 4080 OC 16GB"
            skuCode = "GPU-ASUS-4080"
            quantity = 1
            unitPrice = 450000.00
            lineTotal = 450000.00
        }
    )
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Method POST -Uri $uri -Headers $headers -Body $body
    Write-Host "SUCCESS:" ($res | ConvertTo-Json)
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "ERROR:" $reader.ReadToEnd()
}
