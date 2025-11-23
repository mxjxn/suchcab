// API Client for SuchCab
class API {
  constructor() {
    this.baseURL = '/api';
    this.token = localStorage.getItem('auth_token');
    this.userId = localStorage.getItem('user_id');
  }

  async request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    if (this.token) {
      headers['Authorization'] = `Token ${this.token}`;
    }

    try {
      const response = await fetch(`${this.baseURL}${endpoint}`, {
        ...options,
        headers
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  // Auth
  async login(email, password) {
    const result = await this.request('/user/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });

    if (result.status === 'success') {
      this.token = result.token;
      this.userId = result['user-id'];
      localStorage.setItem('auth_token', this.token);
      localStorage.setItem('user_id', this.userId);
      localStorage.setItem('user_type', result['user-type']);
    }

    return result;
  }

  async register(email, username, password) {
    return await this.request('/user/create', {
      method: 'POST',
      body: JSON.stringify({ email, username, password })
    });
  }

  // Rides
  async createRide(data) {
    return await this.request('/rides/create', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async getRide(id) {
    return await this.request(`/rides/${id}`);
  }

  async getMyRides() {
    return await this.request(`/rides/passenger/${this.userId}`);
  }

  async getActiveRides() {
    return await this.request('/rides/active');
  }

  // Drivers
  async getAvailableDrivers() {
    return await this.request('/drivers/available');
  }

  async getDriver(id) {
    return await this.request(`/drivers/${id}`);
  }

  async getDriverByCard(cardLink) {
    return await this.request(`/drivers/card/${cardLink}`);
  }

  // Messages
  async sendMessage(toUserId, content) {
    return await this.request('/messages/send', {
      method: 'POST',
      body: JSON.stringify({
        'from-user-id': this.userId,
        'to-user-id': toUserId,
        content
      })
    });
  }

  async getUnreadMessages() {
    return await this.request(`/messages/unread/${this.userId}`);
  }

  async getConversation(userId1, userId2) {
    return await this.request(`/messages/conversation?user-id-1=${userId1}&user-id-2=${userId2}`);
  }

  // Contacts
  async addFavorite(driverId) {
    return await this.request('/contacts/add', {
      method: 'POST',
      body: JSON.stringify({
        'passenger-id': this.userId,
        'driver-id': driverId
      })
    });
  }

  async getFavorites() {
    return await this.request(`/contacts/${this.userId}`);
  }

  logout() {
    this.token = null;
    this.userId = null;
    localStorage.clear();
  }
}

const api = new API();
