# Library (REST API)
Api for managing library.
Includes 5 microservices: gateway, lib-service, auth-service, user-service, eureka-server.
And one more library which store common information for other services.

Using technologies:
- jdk-21
- maven
- Sping boot:
    - Web
    - Security
    - DataJPA
        - MySQL
    - Validation
    - Cloud:
        - Gateway
        - Eureka (client, server)
        - Load balancer
    - Test
- JsonWebToken
- Flyway
- Lombok

Testing:
- Unit, integration (Junit, Mockito)
- Postman
- Swagger

## Gateway service
Gateway is common entry point for communication with other services. Locates on http://localhost:8080.
To communicate with other services uses:
-  http://localhost:8080/auth/**  => auth-service
-  http://localhost:8080/lib/**  => lib-service
-  http://localhost:8080/user/**  => user-service

On request gateway will validate path, rebuild path and dispatch to service.

End services may have 4 access levels, which are specified in the path as follows:
- **/r_u/**  -> access for authenticated users and administrator
- **/r_a/**  -> access for administrator
- **/s-i/**  -> endpoints for communication between services, can't be used by users or administrator. No gateway is used for communication between services.
- the remaining paths are open to everyone

To access path's with r_u and r_a, request must have header with jwt token [Authorization: Bearer asdf8723fasdf...]. This will be verified, and the user's ID and role will be determined. If the user does not have access rights, access will be denied. Otherwise, id will be passed to the service request.


## Auth-service
The service stores and allows registration of users, and also issues JWT tokens.

Access abilities:
- Open:
    - Registration. When registering, a new user submits a username (which must not yet exist) and password, which will be stored in the database. The password is stored in encrypted form (BCrypt). It then makes request to the user service, which also creates a user with the same ID in its database. If successful, the response returns a jwt token.
    - Login. When logging in, the user must submit a username and password. If the user exists, a JWT token is returned.
- Admin:
    - the administrator can view the list of all users and delete users.


## Lib-service
The service stores and allows to manage books, authors and genres.

Access abilities:
- Open:
    - all may view books (title, one day rent price, available quantity), authors, and genres.
- Admin:
    - the administrator can add/update/delete books, authors, genres and link them to each other. With internal functions, for example, deleting an author may or may not result in the deletion of all of his books. Or the book cannot be deleted while it is in at least one order.


## User-service
The service stores and allows to manage user accounts and orders.

Access abilities:
- User:
    - can manipulate the account balance.
    - order books and close orders.
- Admin:
    - view accounts, orders.
    - close user orders that are unpaid.

## Launch
- clone from GitHub.
- to build need Maven, JDK-21 and launched MySQL on port 3306.
- on OS Windows may be used file from root directory of the project. Commands to build all services [mvn clean package] or [./build.bat]. If the build is successful, the command to start all services is [./launch.bat]
- or use one of the development environments. The project was developed using Intellij Idea.
- 
If the launch is successful, the registered services can be seen in Spring Eureka http://localhost:8761/

## Testing
The projects are covered with unit and integration tests using the Mockito and Junit frameworks.

Full integration test may be done with Postman:
- import file (collection of tests) from root directory to your Postman: library-test.postman_collection.json
- create a new environment (tests will store variables in it for exchanging information)
- run the tests

The auth-service, user-service, and lib-service endpoints can be tested using the Swagger gateway bypassing the Gateway (without checking the jwt token):
- http://localhost:8083/auth-service/swagger-ui/index.html#/
- http://localhost:8082/user-service/swagger-ui/index.html#/
- http://localhost:8081/lib-service/swagger-ui/index.html#/
