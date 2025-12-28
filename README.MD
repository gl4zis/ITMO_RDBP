# ITMO Refactoring of Databases and Applications course project
**Information Systems course project will be refactored here**

Origin repository here https://github.com/gl4zis/IS-course

## How to start system
1. Install docker
2. Create .env file in project root
3. Fill it like this:
    ```dotenv
    DB_NAME=dormitory
    DB_USER=postgres
    DB_PASS=somePassword
    ```
4. Execute in project root
    ```shell
    docker-compose up -d
    ```
5. Open http://localhost in your browser
6. Enjoy

**Containers:**
- Frontend starts in nginx on 80 port
- Backend starts on 8183 port
- PostgreSQL starts on 5432 port