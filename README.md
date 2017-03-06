# ESSENTIALS

[![Build Status](https://travis-ci.org/zalando-stups/essentials.svg?branch=master)](https://travis-ci.org/zalando-stups/essentials)

ESSENTIALS is the repository for resource types and OAuth 2.0 scopes in the STUPS ecosystem.

## Configuration

ESSENTIALS is based on [friboo-ext-zalando](https://github.com/zalando-incubator/friboo-ext-zalando) and accepts all of its configuration options.
Additionally the following table lists all important config options for this application:

Variable                   | Mandatory? | Default                       | Description
-------------------------- | ---------- | ----------------------------- | -----------
DB_SUBNAME                 | yes        | `//localhost:5432/essentials` | JDBC connection information of your database.
DB_USER                    | yes        | `postgres`                    | Database user.
DB_PASSWORD                | yes        | `postgres`                    | Database password.
API_ALLOWED_UIDS          | yes        | `mjackson,dbowie`             | comma-separated list of user ids that have write access to this API
API_ALLOWED_REALMS        | yes        | `users,robots`                | comma-separated list of realms that have read access to this API
API_VALID_RESOURCE_OWNERS | yes        | `users,robots`                | comma-separated list of resource owners, used for input validation

Example:

```
$ docker run -it \
    -e DB_USER=essentials \
    -e DB_PASSWORD=essentials123 \
    stups/essentials
```
