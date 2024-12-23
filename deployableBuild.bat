rmdir /s /q src\main\resources\static
xcopy ..\pharmacyhub-react\build\ src\main\resources\static\ /E /I
mvn clean package
cd target
java -jar pharmacy-hub.jar