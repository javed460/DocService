# DocService
Spring Boot service exposing REST APIs to upload Excel files and convert them to JSON, and to generate PDFs using PDFBox. Designed for backend integrations and document-processing use cases.


## Tech Stack

- **Java**: 21
- **Spring Boot**: 3.2.2
- **Build Tool**: Maven
- **Libraries**:
    - Apache POI (Excel parsing)
    - Lombok (Boilerplate reduction)
    - SpringDoc OpenAPI (Swagger documentation)

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8089`

## API Documentation

Once the application is running, access the Swagger UI:

```
http://localhost:8089/swagger-ui.html
OR
http://localhost:8089/swagger-ui/index.html