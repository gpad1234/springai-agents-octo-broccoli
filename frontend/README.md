Frontend (React) scaffold

This folder contains a minimal Vite + React scaffold. To develop:

1. cd frontend
2. npm install
3. npm run dev

To build and integrate with Spring Boot (Linux):

```
npm run build
rm -rf ../src/main/resources/static/*
cp -r dist/* ../src/main/resources/static/
```
