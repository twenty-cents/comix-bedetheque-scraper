![Bedetheque Scraper](assets/images/bedetheque-scraper.png)

*Font provided by [SF Slapstick Comic Shaded Oblique](https://www.dafont.com/fr/sf-slapstick-comic.font?text=Bedetheque+Scraper&psize=l&back=theme)*

[![CircleCI](https://img.shields.io/circleci/build/gh/twenty-cents/bedetheque-scraper/develop?token=57ebb4129e2093943ef2a01cccdf64f8c6bec05e)](https://app.circleci.com/pipelines/github/twenty-cents/bedetheque-scraper?branch=develop)

# About
**Bedetheque Scraper** is a Web API which aims to scrape metadata from https://www.bedetheque.com, a french website about comics.

With this API, the following metadata can be retrieved :

- Detailed metadata about : 

    - Series.
    - Graphic novels.
    - Authors.
    - Reviews.
    
- Some global statistics, as total number of series, graphic novels and authors listed.

The API is secure with a JWT token, so you need to sign in before use it.

# Usage

The application uses a local h2 database to store user and trace data. 

These environment variables must be set before launching the application :

| Name                      | Utility |
|---------------------------|---------|
| BS_ACCOUNT_ADMIN_EMAIL    | The email of the admin user.|
| BS_ACCOUNT_ADMIN_PASSWORD | The password of the admin user. |
| BS_SECURITY_JWT-SECRET    | The secret phrase to encode the JWT tokens. |
| BS_SECURITY_JWT_EXPIRE    | The durability of a JWT token before expiration (in millis). |
| BS_DB_USER                | The username to connect to the database. |
| BS_DB_PASSWORD            | The password to connect to the database. |
| BS_FRONT_BASE_URL         | The base url of the application authorized to communicate with the API. |
| BS_CACHE_ACTIVE           | Boolean to (de)active the preloading of all medias scraped from bedetheque on the application server. |

# Swagger
Access the swagger UI for the bedetheque API url with the **dev** profile : http://localhost:8080/swagger-ui/index.html#/
Enter */v3/api-docs/* in the explorer text filter.

Find the API here : http://localhost:8080/v3/api-docs/

# Run
- With maven (dev profile) : *mvn spring-boot:run -Dspring-boot.run.profiles=dev*

# Docker
- Build the image :

```bash
docker build --tag=bedetheque-scraper:latest . 
```
- Run the image : 
```bash
docker run -p8090:8080 bedetheque-scraper:latest
```
- With detached mode : 
```bash
docker run -d -p8090:8080 bedetheque-scraper:latest
docker inspect bedetheque-scraper
docker stop bedetheque-scraper
docker rm bedetheque-scraper
```
- Connect to the API with docker :
  - JSON file : http://localhost:8090/v3/api-docs/
  - Swagger UI : http://localhost:8090/swagger-ui/index.html (v3/api-docs/)

