# PowerShell End-to-End Test Script for PartsHub Item Approval Workflow & Catalog Integration

$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "HHmmss"
$sku1 = "GPU-GIGA-$timestamp"
$sku2 = "COOL-DEEP-$timestamp"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host " STARTING PARTSHUB ITEM APPROVAL & INDEX CATALOG AUDIT " -ForegroundColor Cyan
Write-Host " Testing SKUs: Item 1 ($sku1) & Item 2 ($sku2) " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Role = "Storekeeper"
    )
    $uri = "$baseUrl$Path"
    $headers = @{
        "Content-Type" = "application/json"
        "X-User-Role"  = $Role
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
# 1. STOREKEEPER ADDS NEW ITEM (DEFAULTS TO PENDING)
# -----------------------------------------------------------------
Write-Host "`n[STEP 1] Storekeeper submits new item ($sku1)..." -ForegroundColor Yellow

$newItem1 = @{
    partName = "Gigabyte RTX 4070 Ti Super 16GB - $timestamp"
    skuCode = $sku1
    category = "GPU"
    brand = "Gigabyte"
    compatibility = "PCIe 4.0 ATX"
    supplierName = "Tech Lanka Pvt Ltd"
    qtyOnHand = 15
    reorderLevel = 5
    unitCost = 280000.00
    sellingPrice = 330000.00
    warrantyPeriodMonths = 36
    image = "images/products/rtx4070ti.jpg"
}

$res1 = Invoke-Api -Method "POST" -Path "/api/inventory" -Body $newItem1 -Role "Storekeeper"

if ($res1.Success -and $res1.Data.approvalStatus -eq "PENDING") {
    Write-Host "  [PASS] Item created with PENDING status! ID: $($res1.Data.id) | Status: $($res1.Data.approvalStatus)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Failed to create pending item: $($res1.Error)" -ForegroundColor Red
}

$item1Id = $res1.Data.id

# -----------------------------------------------------------------
# 2. VERIFY PENDING ITEM IS NOT ON INDEX CATALOG (/api/inventory/approved)
# -----------------------------------------------------------------
Write-Host "`n[STEP 2] Verifying pending item is HIDDEN from index.html approved catalog..." -ForegroundColor Yellow

$approvedRes1 = Invoke-Api -Method "GET" -Path "/api/inventory/approved"
$isApproved1 = $approvedRes1.Data | Where-Object { $_.skuCode -eq $sku1 }

if (-not $isApproved1) {
    Write-Host "  [PASS] Pending item ($sku1) is correctly HIDDEN from public approved catalog!" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Pending item was found on public catalog!" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 3. VERIFY BUSINESS OWNER NOTIFICATION
# -----------------------------------------------------------------
Write-Host "`n[STEP 3] Verifying Business Owner alert notification exists..." -ForegroundColor Yellow

$notifRes = Invoke-Api -Method "GET" -Path "/api/notifications" -Role "Business Owner"
$foundNotif = $notifRes.Data.notifications | Where-Object { $_.title -like "*$($newItem1.partName)*" }

if ($foundNotif) {
    Write-Host "  [PASS] Notification generated for Business Owner: $($foundNotif.title)" -ForegroundColor Green
} else {
    Write-Host "  [WARN] Notification for Business Owner not found in recent list." -ForegroundColor Yellow
}

# -----------------------------------------------------------------
# 4. BUSINESS OWNER APPROVES ITEM
# -----------------------------------------------------------------
Write-Host "`n[STEP 4] Business Owner approves Item 1 ($item1Id)..." -ForegroundColor Yellow

$approveRes = Invoke-Api -Method "POST" -Path "/api/inventory/$item1Id/approve" -Role "Business Owner"

if ($approveRes.Success -and $approveRes.Data.approvalStatus -eq "APPROVED") {
    Write-Host "  [PASS] Item 1 approved successfully! Status: $($approveRes.Data.approvalStatus)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Failed to approve item 1: $($approveRes.Error)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 5. VERIFY APPROVED ITEM IS NOW PUBLISHED TO INDEX CATALOG
# -----------------------------------------------------------------
Write-Host "`n[STEP 5] Verifying approved item now appears on index.html catalog..." -ForegroundColor Yellow

$approvedRes2 = Invoke-Api -Method "GET" -Path "/api/inventory/approved"
$isApproved2 = $approvedRes2.Data | Where-Object { $_.skuCode -eq $sku1 }

if ($isApproved2) {
    Write-Host "  [PASS] Item 1 ($sku1) is NOW PUBLISHED to index.html catalog! Name: $($isApproved2.partName) | Image: $($isApproved2.image)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Approved item failed to appear in public catalog!" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 6. STOREKEEPER ADDS ITEM 2 & BUSINESS OWNER REJECTS IT
# -----------------------------------------------------------------
Write-Host "`n[STEP 6] Storekeeper adds Item 2 ($sku2) & Business Owner rejects it..." -ForegroundColor Yellow

$newItem2 = @{
    partName = "DeepCool AK620 Digital CPU Cooler - $timestamp"
    skuCode = $sku2
    category = "Cooling"
    brand = "DeepCool"
    compatibility = "AM5/LGA1700"
    supplierName = "Tech Lanka Pvt Ltd"
    qtyOnHand = 20
    reorderLevel = 5
    unitCost = 18000.00
    sellingPrice = 24000.00
    warrantyPeriodMonths = 24
}

$res2 = Invoke-Api -Method "POST" -Path "/api/inventory" -Body $newItem2 -Role "Storekeeper"
$item2Id = $res2.Data.id

$rejectRes = Invoke-Api -Method "POST" -Path "/api/inventory/$item2Id/reject" -Role "Business Owner"

if ($rejectRes.Success -and $rejectRes.Data.approvalStatus -eq "REJECTED") {
    Write-Host "  [PASS] Item 2 rejected successfully! Status: $($rejectRes.Data.approvalStatus)" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Failed to reject item 2: $($rejectRes.Error)" -ForegroundColor Red
}

# -----------------------------------------------------------------
# 7. VERIFY REJECTED ITEM REMAINS HIDDEN FROM INDEX CATALOG
# -----------------------------------------------------------------
Write-Host "`n[STEP 7] Verifying rejected item ($sku2) remains hidden from index.html catalog..." -ForegroundColor Yellow

$approvedRes3 = Invoke-Api -Method "GET" -Path "/api/inventory/approved"
$isApproved3 = $approvedRes3.Data | Where-Object { $_.skuCode -eq $sku2 }

if (-not $isApproved3) {
    Write-Host "  [PASS] Rejected item ($sku2) remains correctly HIDDEN from public catalog!" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Rejected item appeared in public catalog!" -ForegroundColor Red
}

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host " ITEM APPROVAL & CATALOG INTEGRATION AUDIT COMPLETE " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
