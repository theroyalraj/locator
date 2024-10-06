

# Spring Boot Locator Backend API

This project is a Spring Boot-based backend server that serves geolocation data for food trucks. It uses **Elasticsearch** for geo-location and full text based searches.

## Project Overview

The backend API is responsible for providing geolocation-based search services using **Elasticsearch** as the database. The API allows users to search for food trucks based on latitude and longitude coordinates. The application is deployed using Docker and the backend is hosted at:

👉 [Backend API URL](https://theroyalraj-locator-backend.sliplane.app)

👉 [Swagger UI Documentation](https://theroyalraj-locator-backend.sliplane.app/swagger-ui/index.html)

## Elasticsearch Database

The project uses **Elasticsearch** as the geo-location database for storing and querying coordinates. It is hosted on the **Elast.co Free Tier**, which allows us to perform efficient searches for locations based on geospatial data.


### Elasticsearch Query Example

An example query for finding nearby food trucks based on coordinates looks like this:

```json
{
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query": "your_search_term",
            "fields": [
              "locationDesc",
              "name",
              "permit",
              "description",
              "applicant",
              "optionalText"
            ]
          }
        },
        {
          "term": {
            "dayOrder": {
              "value": "your_day_order_value"
            }
          }
        }
      ],
      "filter": [
        {
          "geo_distance": {
            "distance": "10km",
            "coordinates": {
              "lat": 40.730610,
              "lon": -73.935242
            }
          }
        }
      ]
    }
  },
  "sort": [
    {
      "_geo_distance": {
        "coordinates": {
          "lat": 40.730610,
          "lon": -73.935242
        },
        "order": "asc",
        "unit": "km"
      }
    }
  ],
  "from": 0,  // Your offset
  "size": 10  // Your limit
}

```

## Getting Started

### Requirements

To run this project locally or deploy it, you will need the following:

- **Java 21+**: Ensure you have JDK 21 or higher installed on your machine.
- **Docker**: To containerize and deploy the application easily.
- **Elasticsearch Account**: You can use a free-tier account from [elast.co](https://www.elastic.co/), or set up a local instance of Elasticsearch.

### Running the Application Locally

1. **Clone the repository**:
```bash
git clone https://github.com/theroyalraj/locator.git
cd locator
```


2. **Build the project**:
> ./mvnw clean install

3. **Run the application**:
> ./mvnw spring-boot:run


4. **Access the application**:
API: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui/index.html


## Deployement
Docker Setup
The backend API is containerized using Docker. Here's how you can build and run it using Docker.

Dockerfile
The Dockerfile used to containerize the project:
```declarative
FROM azul/zulu-openjdk:21-latest
VOLUME /tmp
COPY build/libs/*.jar app.jar
EXPOSE 80
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Commands
1. Build the Docker image:
> docker build -t locator-backend .

2. Run the Docker container:
> docker run -p 8080:80 locator-backend


3. Multi-Platform Build & Push
> docker buildx build --platform linux/amd64,linux/arm64 -t theroyalraj/locator-backend:latest --push .


### Deployment on Sliplane
The Docker image is deployed using Sliplane.io, which is a container hosting platform. Here's how you can deploy your image:

Create an account on Sliplane.io.
Push the Docker image to your container registry using the docker buildx command mentioned above.
Deploy your image from Sliplane.io by following their deployment guides and linking your Docker image.
You can now access the deployed backend API at:

#### Developer Details

- ***Name***: Utkarsh Raj
- ***Email***: raj.utkarsh001@gmail.com
- ***LinkedIn***: https://www.linkedin.com/in/theroyalraj/

