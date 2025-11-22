# SuchCab - Tour Booking API

A Clojure-based REST API for managing tour bookings, built with Luminus 3.48.

## Features

- User registration and authentication (JWT-based)
- Tour creation and management
- GraphQL endpoint
- Swagger API documentation

## Prerequisites

- [Leiningen][1] 2.0 or above
- Java 8 or higher

[1]: https://github.com/technomancy/leiningen

## Configuration

### Development

The app reads configuration from `dev-config.edn`. Key settings:

- `:port` - Server port (default: 3000)
- `:nrepl-port` - REPL port (default: 7000)
- `:secret` - JWT encryption secret (change in production!)

### Production

For production, set the `:secret` environment variable:

```bash
export SECRET=$(openssl rand -base64 32)
```

## Running

Start the development server:

```bash
lein run
```

Access the API documentation at: http://localhost:3000/api/api-docs/index.html

## API Endpoints

### Users
- `POST /api/user/create` - Register new user
- `POST /api/user/login` - Authenticate user

### Tours
- `POST /api/tours/create` - Create new tour
- `GET /api/tours/list` - List all tours
- `GET /api/tours/:id` - Get tour by ID

### Utilities
- `GET /api/ping` - Health check
- `POST /api/graphql` - GraphQL endpoint

## Database

Uses Crux as the document database with in-memory storage (development) and persistent event log.

## License

Copyright © 2019
