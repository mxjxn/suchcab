# SuchCab - Pedicab Ride-Sharing Platform

A comprehensive Progressive Web App (PWA) for pedicab rides, tours, and driver-passenger connections. Features a native-like mobile experience with offline support, push notifications, and real-time geolocation. Backend built with Luminus 3.48 (Clojure).

## Features

### Core Features
- **User Management**: Registration and JWT-based authentication for passengers, drivers, and admins
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

### Admin Features
- **Driver Approval System**: Review and approve/reject driver applications
- **Driver Management**: Ban/unban drivers from the platform
- **Business Opportunities**: Create special offers and incentive programs for drivers
- **Smart Assignment**: Assign opportunities to specific drivers or random selection
- **Dashboard Statistics**: Real-time platform metrics and analytics
- **Driver Search**: Find drivers by name, email, or other criteria
- **Detailed Driver Profiles**: View comprehensive driver information including ride history

### Progressive Web App (PWA) Features
- **📱 Installable**: Add to home screen on mobile and desktop
- **⚡ Offline Support**: Service worker caching for offline functionality
- **📍 Geolocation**: Real-time location tracking for rides
- **🔔 Push Notifications**: Get notified about ride updates and messages
- **💾 Background Sync**: Queue actions when offline, sync when online
- **🎨 Native UI**: Mobile-first design with app-like navigation
- **⚙️ App Shortcuts**: Quick actions from home screen icon
- **📶 Offline Indicator**: Clear feedback when connection is lost
- **🔄 Auto-Updates**: Service worker updates for new features

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

### Admin (Requires Admin Authentication)

#### Driver Management
- `GET /api/admin/drivers/pending` - Get pending driver applications
- `GET /api/admin/drivers/approved` - Get approved drivers
- `GET /api/admin/drivers/banned` - Get banned drivers
- `GET /api/admin/drivers/rejected` - Get rejected applications
- `POST /api/admin/drivers/approve` - Approve a driver application
- `POST /api/admin/drivers/reject` - Reject a driver application
- `POST /api/admin/drivers/ban` - Ban a driver from the platform
- `POST /api/admin/drivers/unban` - Unban a driver
- `GET /api/admin/drivers/search` - Search drivers by name/email
- `GET /api/admin/drivers/:driver-id` - Get detailed driver information

#### Business Opportunities
- `POST /api/admin/opportunities/create` - Create business opportunity
- `GET /api/admin/opportunities/list` - List all opportunities
- `GET /api/admin/opportunities/:opportunity-id` - Get opportunity details
- `GET /api/admin/opportunities/:opportunity-id/assignments` - Get opportunity assignments
- `POST /api/admin/opportunities/assign` - Assign to specific driver
- `POST /api/admin/opportunities/assign-random` - Assign to N random drivers
- `POST /api/admin/opportunities/deactivate` - Deactivate an opportunity

#### Platform Statistics
- `GET /api/admin/stats` - Get dashboard statistics

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

### Admin Examples (Requires Admin JWT Token)

#### Approve a Driver Application
```bash
curl -X POST http://localhost:3000/api/admin/drivers/approve \
  -H "Content-Type: application/json" \
  -H "Authorization: Token <admin-jwt-token>" \
  -d '{
    "driver-id": "550e8400-e29b-41d4-a716-446655440000",
    "admin-id": "750e8400-e29b-41d4-a716-446655440000",
    "notes": "Background check passed. Vehicle inspected."
  }'
```

#### Create Business Opportunity
```bash
curl -X POST http://localhost:3000/api/admin/opportunities/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Token <admin-jwt-token>" \
  -d '{
    "title": "Weekend Bonus Program",
    "description": "Earn 20% extra on all weekend rides",
    "type": "bonus",
    "value": 0.20,
    "valid-from": "2025-11-29T00:00:00Z",
    "valid-to": "2025-12-01T23:59:59Z",
    "terms": "Must complete minimum 10 rides"
  }'
```

#### Assign Opportunity to Random Drivers
```bash
curl -X POST http://localhost:3000/api/admin/opportunities/assign-random \
  -H "Content-Type: application/json" \
  -H "Authorization: Token <admin-jwt-token>" \
  -d '{
    "opportunity-id": "850e8400-e29b-41d4-a716-446655440000",
    "count": 25,
    "admin-id": "750e8400-e29b-41d4-a716-446655440000"
  }'
```

#### Get Platform Statistics
```bash
curl -X GET http://localhost:3000/api/admin/stats \
  -H "Authorization: Token <admin-jwt-token>"
```

## Architecture

### Data Models

**Users**: Passengers, drivers, and admins with authentication, avatars, and profiles
**Drivers**: Extended profiles with card links, availability, location, ratings, and approval status (:pending, :approved, :rejected, :banned)
**Rides**: On-demand and scheduled rides with status tracking
**Tours**: Scheduled group tours
**Contacts**: Passenger-driver favorite relationships
**Messages**: Chat messages between users
**Opportunities**: Business opportunities and incentive programs for drivers
**Assignments**: Track which drivers are assigned to which opportunities

### Tech Stack

- **Framework**: Luminus (Ring + Reitit)
- **Database**: Crux (document store with temporal queries)
- **Auth**: Buddy (JWT/JWE encryption)
- **API Docs**: Swagger UI
- **Time**: java-time, clj-time
- **Server**: Jetty

## License

Copyright © 2019
