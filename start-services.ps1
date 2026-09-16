$java = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot\bin\java.exe"
$base = "C:\Users\HP\Desktop\QuestiFy"

# Ensure log directory exists
if (-not (Test-Path "$base\logs")) {
    New-Item -ItemType Directory -Path "$base\logs" -Force | Out-Null
}

$services = @(
    @{ name = "eureka-server"; dir = "$base\questify-backend\questify-backend\eureka-server"; jar = "$base\questify-backend\questify-backend\eureka-server\target\eureka-server.jar"; db = $null },
    @{ name = "auth-service"; dir = "$base\questify-backend\questify-backend\auth-service"; jar = "$base\questify-backend\questify-backend\auth-service\target\auth-service.jar"; db = "questify_auth" },
    @{ name = "institution-service"; dir = "$base\institution-service\institution-service"; jar = "$base\institution-service\institution-service\target\institution-service-0.0.1-SNAPSHOT.jar"; db = "questify_institution" },
    @{ name = "question-bank-service"; dir = "$base\question-bank-service\question-bank-service"; jar = "$base\question-bank-service\question-bank-service\target\question-bank-service-0.0.1-SNAPSHOT.jar"; db = "questify_question_bank" },
    @{ name = "paper-generation-service"; dir = "$base\paper-generation-service\paper-generation-service"; jar = "$base\paper-generation-service\paper-generation-service\target\paper-generation-service-1.0.0.jar"; db = $null },
    @{ name = "papers-service"; dir = "$base\papers-service\papers-service"; jar = "$base\papers-service\papers-service\target\papers-service-1.0.0.jar"; db = "questify_papers" },
    @{ name = "approval-service"; dir = "$base\approval-service\approval-service"; jar = "$base\approval-service\approval-service\target\approval-service-1.0.0.jar"; db = "questify_approvals" },
    @{ name = "backup-service"; dir = "$base\backup-service\backup-service"; jar = "$base\backup-service\backup-service\target\backup-service-1.0.0.jar"; db = "questify_backups" },
    @{ name = "analytics-service"; dir = "$base\analytics-service\analytics-service"; jar = "$base\analytics-service\analytics-service\target\analytics-service-1.0.0.jar"; db = $null },
    @{ name = "audit-log-service"; dir = "$base\audit-log-service\audit-log-service"; jar = "$base\audit-log-service\audit-log-service\target\audit-log-service-1.0.0.jar"; db = $null },
    @{ name = "ml-health-service"; dir = "$base\ml-health-service\ml-health-service"; jar = "$base\ml-health-service\ml-health-service\target\ml-health-service-1.0.0.jar"; db = $null },
    @{ name = "ai-service"; dir = "$base\ai-service\ai-service"; jar = "$base\ai-service\ai-service\target\ai-service-1.0.0.jar"; db = $null },
    @{ name = "api-gateway"; dir = "$base\questify-backend\questify-backend\api-gateway"; jar = "$base\questify-backend\questify-backend\api-gateway\target\api-gateway.jar"; db = $null }
)

Write-Host "=================== 1. STARTING EUREKA SERVER ==================="
$eureka = Start-Process -FilePath $java -ArgumentList "-jar `"$base\questify-backend\questify-backend\eureka-server\target\eureka-server.jar`"" -WorkingDirectory "$base\questify-backend\questify-backend\eureka-server" -RedirectStandardOutput "$base\logs\eureka-server.log" -RedirectStandardError "$base\logs\eureka-server-err.log" -PassThru
Write-Host "Started eureka-server (PID: $($eureka.Id))"

Write-Host "Waiting 6 seconds for Eureka to bind port 8761..."
Start-Sleep -Seconds 6

Write-Host "=================== 2. STARTING MICROSERVICES FLEET ==================="
foreach ($s in $services) {
    if ($s.name -eq "eureka-server") { continue }
    
    $p = Start-Process -FilePath $java -ArgumentList "-jar `"$($s.jar)`"" -WorkingDirectory $s.dir -RedirectStandardOutput "$base\logs\$($s.name).log" -RedirectStandardError "$base\logs\$($s.name)-err.log" -PassThru
    Write-Host "Started $($s.name) (PID: $($p.Id))"
    Start-Sleep -Milliseconds 1500
}
Write-Host "All services started."
