# SuchCab - Pedicab Ride-Sharing Platform

A comprehensive Clojure-based REST API for managing pedicab rides, tours, and driver-passenger connections. Built with Luminus 3.48.

## Features

### Core Features
- **User Management**: Registration and JWT-based authentication for both passengers and drivers
- **On-Demand Rides**: Real-time ride requests with driver matching
- **Scheduled Rides**: Book rides in advance
- **Tour Management**: Create and manage scheduled group tours
- **Driver Profiles**: Personal driver "cards" with shareable links
- **Driver Availability**: Real-time tracking of available pedicabs in your area
- **Contacts/Favorites**: Save preferred drivers for quick bookings
- **Messaging**: In-app chat between drivers and passengers
- **User Avatars**: Photo profile support for personalization
- **GraphQL Support**: Alternative query interface
- **Auto-generated Documentation**: Interactive Swagger UI

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
- `POST /api/user/create` - Register new user (passenger or driver)
- `POST /api/user/login` - Authenticate user and receive JWT token
- `PUT /api/user/avatar` - Update user avatar
- `GET /api/user/:user-id` - Get user profile

### Drivers
- `POST /api/drivers/create` - Create/update driver profile with card link
- `GET /api/drivers/:id` - Get driver by ID
- `GET /api/drivers/card/:card-link` - Get driver by personal card link
- `PUT /api/drivers/availability` - Update driver availability and location
- `GET /api/drivers/available` - Get all currently available drivers
- `GET /api/drivers/list` - List all drivers

### Rides (On-Demand & Scheduled)
- `POST /api/rides/create` - Create new ride request
- `GET /api/rides/:id` - Get ride details
- `PUT /api/rides/status` - Update ride status
- `POST /api/rides/accept` - Driver accepts a ride
- `POST /api/rides/complete` - Complete ride and add rating
- `GET /api/rides/passenger/:passenger-id` - Get all rides for passenger
- `GET /api/rides/driver/:driver-id` - Get all rides for driver
- `GET /api/rides/active` - Get all active rides
- `GET /api/rides/scheduled` - Get upcoming scheduled rides

### Tours (Scheduled Group Tours)
- `POST /api/tours/create` - Create new tour offering
- `GET /api/tours/list` - List all tours
- `GET /api/tours/:id` - Get tour by ID

### Contacts (Favorite Drivers)
- `POST /api/contacts/add` - Add driver to favorites
- `DELETE /api/contacts/remove` - Remove driver from favorites
- `GET /api/contacts/:passenger-id` - Get all favorite drivers
- `GET /api/contacts/check` - Check if driver is favorited

### Messages (Chat)
- `POST /api/messages/send` - Send message to another user
- `PUT /api/messages/read` - Mark message as read
- `GET /api/messages/conversation` - Get conversation between two users
- `GET /api/messages/ride/:ride-id` - Get all messages for a ride
- `GET /api/messages/unread/:user-id` - Get unread messages
- `GET /api/messages/conversations/:user-id` - Get list of conversation partners

### Utilities
- `GET /api/ping` - Health check
- `POST /api/graphql` - GraphQL endpoint

## Database

Uses Crux as the document database with in-memory storage (development) and persistent event log.

## Usage Examples

### Create a Driver Profile with Card
```bash
curl -X POST http://localhost:3000/api/drivers/create \
  -H "Content-Type: application/json" \
  -d '{
    "user-id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "John Driver",
    "bio": "Experienced pedicab driver in Boston",
    "vehicle-info": {"type": "pedicab", "color": "red"},
    "card-slug": "john-driver"
  }'
```

### Request an On-Demand Ride
```bash
curl -X POST http://localhost:3000/api/rides/create \
  -H "Content-Type: application/json" \
  -d '{
    "passenger-id": "650e8400-e29b-41d4-a716-446655440000",
    "pickup-location": {"lat": 42.3601, "lon": -71.0589},
    "dropoff-location": {"lat": 42.3656, "lon": -71.0614},
    "ride-type": "on-demand"
  }'
```

### Schedule a Future Ride
```bash
curl -X POST http://localhost:3000/api/rides/create \
  -H "Content-Type: application/json" \
  -d '{
    "passenger-id": "650e8400-e29b-41d4-a716-446655440000",
    "pickup-location": {"lat": 42.3601, "lon": -71.0589},
    "dropoff-location": {"lat": 42.3656, "lon": -71.0614},
    "ride-type": "scheduled",
    "scheduled-time": "2025-11-24T18:00:00Z"
  }'
```

### Check Available Drivers
```bash
curl -X GET http://localhost:3000/api/drivers/available
```

### Add Driver to Favorites
```bash
curl -X POST http://localhost:3000/api/contacts/add \
  -H "Content-Type: application/json" \
  -d '{
    "passenger-id": "650e8400-e29b-41d4-a716-446655440000",
    "driver-id": "550e8400-e29b-41d4-a716-446655440000",
    "notes": "Best driver in Boston!"
  }'
```

### Send a Message
```bash
curl -X POST http://localhost:3000/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "from-user-id": "650e8400-e29b-41d4-a716-446655440000",
    "to-user-id": "550e8400-e29b-41d4-a716-446655440000",
    "content": "On my way to pickup location!"
  }'
```

## Architecture

### Data Models

**Users**: Passengers and drivers with authentication, avatars, and profiles
**Drivers**: Extended profiles with card links, availability, location, and ratings
**Rides**: On-demand and scheduled rides with status tracking
**Tours**: Scheduled group tours
**Contacts**: Passenger-driver favorite relationships
**Messages**: Chat messages between users

### Tech Stack

- **Framework**: Luminus (Ring + Reitit)
- **Database**: Crux (document store with temporal queries)
- **Auth**: Buddy (JWT/JWE encryption)
- **API Docs**: Swagger UI
- **Time**: java-time, clj-time
- **Server**: Jetty

## License

Copyright © 2019
