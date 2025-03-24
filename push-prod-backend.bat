@echo off
REM === Set variables ===
set DOCKER_IMAGE=syedus06/pharmacyhub-backend
set TAG=prod
set ENV_FILE=deploy/env/.env.prod

REM === Navigate to backend directory ===
cd /d D:\code\PharmacyHub\pharmacyhub-backend

REM === Authenticate with Docker Hub (requires prior login or credential store) ===
echo Logging into Docker Hub...
docker login || exit /b

REM === Build Docker image ===
echo Building image...
docker build -t %DOCKER_IMAGE%:%TAG% --build-arg ENV_FILE=%ENV_FILE% -f Dockerfile .

REM === Push Docker image ===
echo Pushing image to Docker Hub...
docker push %DOCKER_IMAGE%:%TAG%

echo Done: Image %DOCKER_IMAGE%:%TAG% built and pushed.
pause
