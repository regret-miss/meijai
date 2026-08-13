param(
    [string]$BaseUrl = 'http://127.0.0.1:8082',
    [Parameter(Mandatory = $true)][string]$MemberUsername,
    [Parameter(Mandatory = $true)][string]$MemberPassword,
    [Parameter(Mandatory = $true)][string]$AdminUsername,
    [Parameter(Mandatory = $true)][string]$AdminPassword
)

$ErrorActionPreference = 'Stop'
$failures = [System.Collections.Generic.List[string]]::new()

function Assert-True([bool]$Condition, [string]$Message) {
    if (-not $Condition) { $failures.Add($Message) }
}

$homeName = ([char]0x9996) + ([char]0x9875) + '.html'
$loginName = ([char]0x767B) + ([char]0x5F55) + '.html'
foreach ($path in @("/nail-site/$homeName", "/nail-site/$loginName", '/nail-site/AI.html', '/admin/index.html')) {
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri ($BaseUrl + $path)
        Assert-True ($response.StatusCode -eq 200) "Page is unavailable: $path"
    } catch {
        $failures.Add("Page is unavailable: $path")
    }
}

$memberSession = [Microsoft.PowerShell.Commands.WebRequestSession]::new()
$before = Invoke-RestMethod -Uri "$BaseUrl/api/nail/auth/session" -WebSession $memberSession
Assert-True ($before.code -eq 200 -and $before.data.loggedIn -eq $false) 'Anonymous session check failed'

$badLogin = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/nail/auth/login" -ContentType 'application/json' -Body (@{
    username = $MemberUsername
    password = 'incorrect-password'
} | ConvertTo-Json) -WebSession $memberSession
Assert-True ($badLogin.code -ne 200) 'Invalid member password was not rejected'

$memberLogin = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/nail/auth/login" -ContentType 'application/json' -Body (@{
    username = $MemberUsername
    password = $MemberPassword
} | ConvertTo-Json) -WebSession $memberSession
$after = Invoke-RestMethod -Uri "$BaseUrl/api/nail/auth/session" -WebSession $memberSession
Assert-True ($memberLogin.code -eq 200 -and $after.data.loggedIn -eq $true) 'Member login or session validation failed'

$adminLogin = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/system/login" -ContentType 'application/json' -Body (@{
    username = $AdminUsername
    password = $AdminPassword
    code = ''
    uuid = ''
} | ConvertTo-Json)
Assert-True ($adminLogin.code -eq 200 -and -not [string]::IsNullOrWhiteSpace($adminLogin.data.token)) 'Admin login failed'

if ($adminLogin.code -eq 200) {
    $headers = @{ 'like-admin' = $adminLogin.data.token }
    $assets = Invoke-RestMethod -Uri "$BaseUrl/api/nail/asset/list?pageNo=1&pageSize=10" -Headers $headers
    $stats = Invoke-RestMethod -Uri "$BaseUrl/api/nail/ai/stats" -Headers $headers
    Assert-True ($assets.code -eq 200) 'Asset list endpoint failed'
    Assert-True ($stats.code -eq 200) 'AI stats endpoint failed'
}

$allowed = Invoke-WebRequest -UseBasicParsing -Method Options -Uri "$BaseUrl/api/nail/auth/session" -Headers @{
    Origin = 'http://127.0.0.1:8765'
    'Access-Control-Request-Method' = 'GET'
}
Assert-True ($allowed.Headers['Access-Control-Allow-Origin'] -eq 'http://127.0.0.1:8765') 'Local frontend CORS origin was not allowed'

$blockedOrigin = $null
try {
    $blocked = Invoke-WebRequest -UseBasicParsing -Method Options -Uri "$BaseUrl/api/nail/auth/session" -Headers @{
        Origin = 'https://example.invalid'
        'Access-Control-Request-Method' = 'GET'
    }
    $blockedOrigin = $blocked.Headers['Access-Control-Allow-Origin']
} catch {
    if ($_.Exception.Response) {
        $blockedOrigin = $_.Exception.Response.Headers['Access-Control-Allow-Origin']
    }
}
Assert-True ([string]::IsNullOrWhiteSpace($blockedOrigin)) 'Unlisted CORS origin was allowed'

$homeSource = Get-Content (Join-Path $PSScriptRoot ("..\frontend\nail-site\$homeName")) -Raw -Encoding utf8
$aiSource = Get-Content (Join-Path $PSScriptRoot '..\frontend\nail-site\js\ai-studio.js') -Raw -Encoding utf8
Assert-True ($homeSource.Contains('NailMemberAuth.isLoggedIn')) 'Homepage auth guard is missing'
Assert-True (-not $homeSource.Contains("AI.html?q=")) 'Homepage still exposes prompts in the URL'
Assert-True ($aiSource.Contains('takePrompt')) 'AI prompt handoff is missing'

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Output 'Smoke test passed: pages, member auth, admin auth, assets, AI stats, CORS and prompt handoff.'
