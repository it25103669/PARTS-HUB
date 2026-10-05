# PowerShell End-to-End Test Script for PartsHub Returns & Warranty Management Use Case

$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "HHmmss"
$testInv = "INV-2026-$timestamp"
$oldInv = "INV-OLD-$timestamp"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host " STARTING PARTSHUB RETURNS & WARRANTY USE CASE END-TO-END AUDIT " -ForegroundColor Cyan
Write-Host " Testing with Invoice: $testInv and Old Invoice: $oldInv " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null
    )
    $uri = "$baseUrl$Path"
    $headers = @{
        "Content-Type" = "application/json"
        "X-User-Role"  = "Business Owner"
    }

    try {
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 5 -Compress
            $res = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -Body $jsonBody
        } else {
            $res = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers
        }
        return @{ Success = $true; Data = $res; StatusCode = 200 }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errText = $reader.ReadToEnd()
        return @{ Success = $false; StatusCode = $statusCode; Error = $errText }
    }
}

# -----------------------------------------------------------------
# 0. SEEDING / VERIFYING INVENTORY ITEMS & SALE INVOICE
# -----------------------------------------------------------------
Write-Host "`n[STEP 0] Ensuring valid Inventory Items & Sale Invoice ($testInv) exist..." -ForegroundColor Yellow

$invItem1 = @{
    partName = "ASUS ROG Strix RTX 4080 OC 16GB"
    skuCode = "GPU-ASUS-4080"
    category = "Graphics Cards"
    brand = "ASUS"
    compatibility = "PCIe 4.0 ATX"
    supplierName = "Nanotech Hardware"
    qtyOnHand = 100
    reorderLevel = 5
    unitCost = 380000.00
    sellingPrice = 450000.00
    warrantyPeriodMonths = 12
}
$seedInvRes1 = Invoke-Api -Method "POST" -Path "/api/inventory" -Body $invItem1
if ($seedInvRes1.Success -and $seedInvRes1.Data.id) {
    # Ensure stock is high
    $stockPayload = @{ quantity = 100 }
    Invoke-Api -Method "POST" -Path "/api/inventory/$($seedInvRes1.Data.id)/stock" -Body $stockPayload | Out-Null
}

$invItem2 = @{
    partName = "Corsair RM850x 850W Gold PSU"
    skuCode = "PSU-CORSAIR-850"
    category = "Power Supplies"
    brand = "Corsair"
    compatibility = "ATX Standard"
    supplierName = "Nanotech Hardware"
    qtyOnHand = 100
    reorderLevel = 5
    unitCost = 45000.00
    sellingPrice = 55000.00
    warrantyPeriodMonths = 12
}
$seedInvRes2 = Invoke-Api -Method "POST" -Path "/api/inventory" -Body $invItem2
if ($seedInvRes2.Success -and $seedInvRes2.Data.id) {
    $stockPayload = @{ quantity = 100 }
    Invoke-Api -Method "POST" -Path "/api/inventory/$($seedInvRes2.Data.id)/stock" -Body $stockPayload | Out-Null
}

$salePayload = @{
    invoiceNumber = $testInv
    customerName = "Colombo PC Clinic"
    customerContact = "0771234567"
    saleDate = "2026-09-01"
    paymentMethod = "Cash"
    paymentStatus = "PAID"
    items = @(
        @{
            partName = "ASUS ROG Strix RTX 4080 OC 16GB"
            skuCode = "GPU-ASUS-4080"
            quantity = 3
            unitPrice = 450000.00
            lineTotal = 1350000.00
        }
    )
}
$seedRes = Invoke-Api -Method "POST" -Path "/api/sales" -Body $salePayload
if ($seedRes.Success) {
    Write-Host "  -> Sale Created: $($seedRes.Data.invoiceNumber)" -ForegroundColor Green
} else {
    Write-Host "  -> Seed Sale Error: $($seedRes.Error)" -ForegroundColor Red
}

$lookupRes = Invoke-Api -Method "GET" -Path "/api/returns/lookup-sale?invoiceNumber=$testInv&customerName=Colombo%20PC%20Clinic"
if ($lookupRes.Success) {
    Write-Host "  -> Sale Lookup SUCCESS: Invoice $testInv verified for Colombo PC Clinic." -ForegroundColor Green
    Write-Host "     Line Item: $($lookupRes.Data.items[0].partName) | Returnable Qty: $($lookupRes.Data.items[0].returnableQuantity) | InWarranty: $($lookupRes.Data.items[0].inWarranty)" -ForegroundColor Gray
} else {
    Write-Host "  -> Sale Lookup Error: $($lookupRes.Error)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 1. LOG VALID RETURN CLAIM (IN-WARRANTY)
# -----------------------------------------------------------------
Write-Host "`n[TEST 1] Logging valid return claim against $testInv..." -ForegroundColor Yellow
$claim1 = @{
    claimType = "CUSTOMER_RETURN"
    referenceNumber = $testInv
    partyName = "Colombo PC Clinic"
    partName = "ASUS ROG Strix RTX 4080 OC 16GB"
    quantity = 1
    itemCondition = "DEFECTIVE"
    reason = "Defective"
    resolution = "Refund"
    diagnosticNotes = "Coil whine and fan noise under full synthetic stress benchmark."
}
$res1 = Invoke-Api -Method "POST" -Path "/api/returns" -Body $claim1
if ($res1.Success -and $res1.Data.claimNumber) {
    Write-Host "  [PASS] Valid Return Claim Created: $($res1.Data.claimNumber) | Status: $($res1.Data.status) | InWarranty: $($res1.Data.inWarranty)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Could not create valid return claim: $($res1.Error)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 2. MISMATCHED / NONEXISTENT SALE RECORD VALIDATION (EXCEPTION FLOW 1)
# -----------------------------------------------------------------
Write-Host "`n[TEST 2] Testing sale record validation (Nonexistent / Mismatched)..." -ForegroundColor Yellow
$badClaim1 = @{
    claimType = "CUSTOMER_RETURN"
    referenceNumber = "INV-99999"
    partyName = "Colombo PC Clinic"
    partName = "ASUS ROG Strix RTX 4080 OC 16GB"
    quantity = 1
    reason = "Defective"
    resolution = "Refund"
}
$resBad1 = Invoke-Api -Method "POST" -Path "/api/returns" -Body $badClaim1
if (-not $resBad1.Success -and $resBad1.Error -like "*Invalid Sale Record*") {
    Write-Host "  [PASS] Nonexistent Sale correctly BLOCKED: $($resBad1.Error)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Nonexistent Sale was not blocked correctly: StatusCode $($resBad1.StatusCode)" -ForegroundColor Red
}

$badClaim2 = @{
    claimType = "CUSTOMER_RETURN"
    referenceNumber = $testInv
    partyName = "Wrong Customer Name"
    partName = "ASUS ROG Strix RTX 4080 OC 16GB"
    quantity = 1
    reason = "Defective"
    resolution = "Refund"
}
$resBad2 = Invoke-Api -Method "POST" -Path "/api/returns" -Body $badClaim2
if (-not $resBad2.Success -and $resBad2.Error -like "*Invalid Customer Details*") {
    Write-Host "  [PASS] Mismatched Customer Name correctly BLOCKED: $($resBad2.Error)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Mismatched Customer Name was not blocked correctly: StatusCode $($resBad2.StatusCode)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 3. RETURNABLE QUANTITY VALIDATION (EXCEPTION FLOW 3)
# -----------------------------------------------------------------
Write-Host "`n[TEST 3] Testing returnable-quantity validation..." -ForegroundColor Yellow
$excessClaim = @{
    claimType = "CUSTOMER_RETURN"
    referenceNumber = $testInv
    partyName = "Colombo PC Clinic"
    partName = "ASUS ROG Strix RTX 4080 OC 16GB"
    quantity = 50
    reason = "Defective"
    resolution = "Refund"
}
$resExcess = Invoke-Api -Method "POST" -Path "/api/returns" -Body $excessClaim
if (-not $resExcess.Success -and $resExcess.Error -like "*Return Quantity Exceeded*") {
    Write-Host "  [PASS] Exceeded Quantity correctly REJECTED: $($resExcess.Error)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Exceeded Quantity was not rejected correctly: StatusCode $($resExcess.StatusCode)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 4. OUT OF WARRANTY & GOODWILL OVERRIDE TRACKING (ALTERNATE FLOW 1)
# -----------------------------------------------------------------
Write-Host "`n[TEST 4] Testing out-of-warranty goodwill override requirement..." -ForegroundColor Yellow
$oldSalePayload = @{
    invoiceNumber = $oldInv
    customerName = "Apex Gaming Hub"
    customerContact = "0719876543"
    saleDate = "2024-01-15"
    paymentMethod = "Credit Card"
    paymentStatus = "PAID"
    items = @(
        @{
            partName = "Corsair RM850x 850W Gold PSU"
            skuCode = "PSU-CORSAIR-850"
            quantity = 1
            unitPrice = 55000.00
            lineTotal = 55000.00
        }
    )
}
$seedOldRes = Invoke-Api -Method "POST" -Path "/api/sales" -Body $oldSalePayload

$noGoodwillClaim = @{
    claimType = "CUSTOMER_RETURN"
    referenceNumber = $oldInv
    partyName = "Apex Gaming Hub"
    partName = "Corsair RM850x 850W Gold PSU"
    quantity = 1
    reason = "Defective"
    resolution = "Refund"
    goodwillOverride = $false
}
$resNoGoodwill = Invoke-Api -Method "POST" -Path "/api/returns" -Body $noGoodwillClaim
if (-not $resNoGoodwill.Success -and $resNoGoodwill.Error -like "*Warranty Expired*") {
    Write-Host "  [PASS] Out-of-warranty return without goodwill override correctly BLOCKED: $($resNoGoodwill.Error)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Out-of-warranty return without goodwill was not blocked: StatusCode $($resNoGoodwill.StatusCode) Error: $($resNoGoodwill.Error)" -ForegroundColor Red
}

$goodwillClaim = @{
    claimType = "CUSTOMER_RETURN"
    referenceNumber = $oldInv
    partyName = "Apex Gaming Hub"
    partName = "Corsair RM850x 850W Gold PSU"
    quantity = 1
    reason = "Defective"
    resolution = "Refund"
    goodwillOverride = $true
    goodwillReason = "Store Manager VIP Client Goodwill Exception"
    diagnosticNotes = "PSU fan bearing failure after 2.5 years."
}
$resGoodwill = Invoke-Api -Method "POST" -Path "/api/returns" -Body $goodwillClaim
if ($resGoodwill.Success -and $resGoodwill.Data.goodwillOverride -eq $true) {
    Write-Host "  [PASS] Out-of-warranty return with Goodwill Override APPROVED: $($resGoodwill.Data.claimNumber) | Goodwill Reason: $($resGoodwill.Data.goodwillReason)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Goodwill override claim failed: $($resGoodwill.Error)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 5. REJECT CLAIM FOR CUSTOMER MISUSE (EXCEPTION FLOW 4)
# -----------------------------------------------------------------
Write-Host "`n[TEST 5] Testing claim rejection for customer misuse..." -ForegroundColor Yellow
$misuseClaim = @{
    claimType = "CUSTOMER_RETURN"
    referenceNumber = $testInv
    partyName = "Colombo PC Clinic"
    partName = "ASUS ROG Strix RTX 4080 OC 16GB"
    quantity = 1
    reason = "CUSTOMER_MISUSE"
    resolution = "Reject - Customer Misuse"
    rejectionReason = "PCB physical crack and water damage near PCIe slot."
    diagnosticNotes = "Inspection bench confirmed liquid residue inside heatsink."
}
$resMisuse = Invoke-Api -Method "POST" -Path "/api/returns" -Body $misuseClaim
if ($resMisuse.Success -and $resMisuse.Data.status -eq "REJECTED") {
    Write-Host "  [PASS] Customer Misuse Claim REJECTED: $($resMisuse.Data.claimNumber) | Status: $($resMisuse.Data.status) | Rejection Reason: $($resMisuse.Data.rejectionReason)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Customer Misuse Claim failed: $($resMisuse.Error)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 6. SUPPLIER ROUTING FOR MANUFACTURING DEFECTS (STEP 6)
# -----------------------------------------------------------------
Write-Host "`n[TEST 6] Testing supplier routing for manufacturing defect returns..." -ForegroundColor Yellow
$defectClaim = @{
    claimType = "WARRANTY_CLAIM"
    referenceNumber = $testInv
    partyName = "Colombo PC Clinic"
    partName = "ASUS ROG Strix RTX 4080 OC 16GB"
    quantity = 1
    reason = "MANUFACTURING_DEFECT"
    resolution = "Replacement"
    diagnosticNotes = "Factory solder cold joint on VRM power phase."
}
$resDefect = Invoke-Api -Method "POST" -Path "/api/returns" -Body $defectClaim
if ($resDefect.Success -and $resDefect.Data.status -eq "PENDING_SUPPLIER_CLAIM") {
    Write-Host "  [PASS] Manufacturing Defect claim routed to Supplier: $($resDefect.Data.claimNumber) | Status: $($resDefect.Data.status) | Supplier: $($resDefect.Data.supplierName)" -ForegroundColor Green

    $claimId = $resDefect.Data.id
    $resolvePayload = @{
        resolution = "Supplier Full Credit Note and Fresh Unit Issued"
        notes = "Nanotech Hardware accepted RMA return ticket #88941."
    }
    $resSupplierResolve = Invoke-Api -Method "POST" -Path "/api/returns/$claimId/resolve-supplier-claim" -Body $resolvePayload
    if ($resSupplierResolve.Success -and $resSupplierResolve.Data.status -eq "RESOLVED") {
        Write-Host "  [PASS] Supplier Claim RESOLVED successfully: Status: $($resSupplierResolve.Data.status) | Resolution: $($resSupplierResolve.Data.resolution)" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Failed to resolve supplier claim: $($resSupplierResolve.Error)" -ForegroundColor Red
    }
} else {
    Write-Host "  [FAIL] Manufacturing Defect routing failed: $($resDefect.Error)" -ForegroundColor Red
}

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host " RETURNS & WARRANTY MANAGEMENT END-TO-END AUDIT COMPLETE " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
