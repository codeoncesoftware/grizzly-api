# Grizzly API
Grizzly API, Next Gen Microservice Framework, designed for Web and Mobile developers    

1. API First with OpenAPI (Swagger)
2. Secured API with JWT & JWS
3. Media API (Images, PDF, Word, excel,...)
4. NoSQL runtime engine MongoDB
5. Markup transformation engine Freemarker, Thymeleaf, XSL   

Design your API be defining multiple highly-configurable endpoints.  
---   
# Instructions

### 1. Run a local ```MongoDB``` instance available for port 27017   
###### Docker command :   
```
docker pull mongo   
docker run -d -p 27017:27017 --name mongodb mongo
```
Or manually install from [MongoDB](https://www.mongodb.com/download-center/community)   

### 2. Clone this repository   
```git clone https://github.com/codeoncesoftware/grizzly-api.git```   

### 3. RUN the 3 microservices (gateway, core and runtime)   
###### With IDE : 
Import ```grizzly-api-core```, ```grizzly-api-gateway```, ```grizzly-api-common``` and ```grizzly-api-runtime``` in your favourite IDE as maven projects and Run the 3 maven projects.   
###### Commands:   
For each microservice open a new terminal tab   
```
// tab 1
cd grizzly-api-gateway   
mvn spring-boot:run

// tab 2
cd grizzly-api-core
cd grizzly-api-rest
mvn spring-boot:run

// tab 3
cd grizzly-api-runtime   
mvn spring-boot:run
```
### 4. Go to ```grizzly-api-angular``` folder, install the dependencies with ```npm install``` and start the Angular server with ```ng serve``` command.     
###### Commands:   
```
cd grizzly-api   
cd grizzly-api-angular   
ng serve
```

### 5. Open your browser and go to : http://localhost:4200   

### 6. Default credentials for Login are : ```username: admin``` and ```password: admin123```

### 7. Create your first API   :rocket:

For more informations, go to [Grizzly API](https://www.grizzly-api.com) or contact us on support@grizzly-api.com


---

# License  
Grizzly API Open Source software released under the [Apache 2.0 license](LICENSE).
