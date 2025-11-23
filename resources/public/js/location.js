// Geolocation utilities for SuchCab
class LocationService {
  constructor() {
    this.currentPosition = null;
    this.watchId = null;
  }

  async getCurrentPosition() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.currentPosition = {
            lat: position.coords.latitude,
            lon: position.coords.longitude,
            accuracy: position.coords.accuracy
          };
          resolve(this.currentPosition);
        },
        (error) => {
          reject(this.handleError(error));
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
      );
    });
  }

  watchPosition(callback) {
    if (!navigator.geolocation) {
      console.error('Geolocation not supported');
      return;
    }

    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        this.currentPosition = {
          lat: position.coords.latitude,
          lon: position.coords.longitude,
          accuracy: position.coords.accuracy,
          heading: position.coords.heading,
          speed: position.coords.speed
        };
        callback(this.currentPosition);
      },
      (error) => {
        console.error('Watch position error:', this.handleError(error));
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0
      }
    );
  }

  stopWatching() {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
  }

  handleError(error) {
    switch (error.code) {
      case error.PERMISSION_DENIED:
        return new Error('Location permission denied');
      case error.POSITION_UNAVAILABLE:
        return new Error('Location information unavailable');
      case error.TIMEOUT:
        return new Error('Location request timed out');
      default:
        return new Error('Unknown location error');
    }
  }

  calculateDistance(lat1, lon1, lat2, lon2) {
    // Haversine formula
    const R = 6371; // Earth's radius in km
    const dLat = this.toRadians(lat2 - lat1);
    const dLon = this.toRadians(lon2 - lon1);

    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(this.toRadians(lat1)) * Math.cos(this.toRadians(lat2)) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // Distance in km
  }

  toRadians(degrees) {
    return degrees * (Math.PI / 180);
  }

  formatDistance(km) {
    if (km < 1) {
      return `${Math.round(km * 1000)}m`;
    }
    return `${km.toFixed(1)}km`;
  }
}

const locationService = new LocationService();
