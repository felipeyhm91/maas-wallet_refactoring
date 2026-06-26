# Script de Inicialização Automatizada - MaaS Wallet

$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "         Inicializando Plataforma MaaS Wallet" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Configurar JAVA_HOME
$JavaPath = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
if (Test-Path $JavaPath) {
    $env:JAVA_HOME = $JavaPath
    Write-Host "[OK] JAVA_HOME configurado para: $JavaPath" -ForegroundColor Green
} else {
    Write-Host "[ERRO] JDK 21 não encontrado no caminho padrão: $JavaPath" -ForegroundColor Red
    Write-Host "Por favor, verifique se instalou o JDK 21 do OpenJDK Microsoft." -ForegroundColor Yellow
    exit 1
}

# 2. Configurar Maven
$MavenCmd = "C:\Users\felip\.gemini\antigravity\scratch\maven\apache-maven-3.9.6\bin\mvn.cmd"
if (Test-Path $MavenCmd) {
    Write-Host "[OK] Maven local encontrado em: $MavenCmd" -ForegroundColor Green
} else {
    # Tenta usar maven do PATH global
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        $MavenCmd = "mvn"
        Write-Host "[OK] Maven global encontrado no PATH." -ForegroundColor Green
    } else {
        Write-Host "[ERRO] Apache Maven não encontrado. Execute o plano de instalação do Maven primeiro." -ForegroundColor Red
        exit 1
    }
}

# 3. Compilar e Executar
Write-Host "`nCompilando e executando a aplicação..." -ForegroundColor Cyan
& $MavenCmd spring-boot:run
