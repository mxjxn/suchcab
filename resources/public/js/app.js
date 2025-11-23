// Main Application Logic
class SuchCabApp {
  constructor() {
    this.currentPage = 'home';
    this.deferredPrompt = null;
    this.init();
  }

  async init() {
    // Register service worker
    if ('serviceWorker' in navigator) {
      try {
        await navigator.serviceWorker.register('/sw.js');
        console.log('Service Worker registered');
      } catch (error) {
        console.error('SW registration failed:', error);
      }
    }

    // Setup event listeners
    this.setupEventListeners();

    // Check authentication
    if (api.token) {
      this.showApp();
    } else {
      this.showLoginPage();
    }

    // Request notification permission
    this.requestNotificationPermission();

    // Hide splash screen
    setTimeout(() => {
      document.getElementById('splash').style.opacity = '0';
      setTimeout(() => {
        document.getElementById('splash').classList.add('hidden');
      }, 300);
    }, 1000);
  }

  setupEventListeners() {
    // Menu button
    document.getElementById('menu-btn').addEventListener('click', () => UI.toggleMenu());

    // Close menu when clicking outside
    document.addEventListener('click', (e) => {
      const menu = document.getElementById('side-menu');
      const menuBtn = document.getElementById('menu-btn');
      if (!menu.contains(e.target) && !menuBtn.contains(e.target)) {
        UI.closeMenu();
      }
    });

    // Navigation
    document.querySelectorAll('[data-page]').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const page = e.currentTarget.dataset.page;
        this.navigateTo(page);
      });
    });

    // Logout
    document.getElementById('logout-btn').addEventListener('click', () => this.logout());

    // PWA install prompt
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      this.deferredPrompt = e;
      this.showInstallPrompt();
    });

    // Online/Offline status
    window.addEventListener('online', () => this.updateOnlineStatus(true));
    window.addEventListener('offline', () => this.updateOnlineStatus(false));
  }

  showApp() {
    document.getElementById('app').classList.remove('hidden');
    this.navigateTo('home');
  }

  showLoginPage() {
    UI.renderPage('login', `
      <div class="card" style="max-width: 400px; margin: 40px auto;">
        <h2 style="margin-bottom: 24px;">Welcome to SuchCab</h2>
        <form id="login-form">
          <div class="form-group">
            <label class="form-label">Email</label>
            <input type="email" class="form-input" name="email" required>
          </div>
          <div class="form-group">
            <label class="form-label">Password</label>
            <input type="password" class="form-input" name="password" required>
          </div>
          <button type="submit" class="btn btn-primary btn-block">Login</button>
        </form>
        <p style="text-align: center; margin-top: 16px;">
          Don't have an account? <a href="#" id="show-register">Register</a>
        </p>
      </div>
    `);

    document.getElementById('login-form').addEventListener('submit', (e) => this.handleLogin(e));
    document.getElementById('show-register').addEventListener('click', (e) => {
      e.preventDefault();
      this.showRegisterPage();
    });
  }

  async handleLogin(e) {
    e.preventDefault();
    const form = e.target;
    const email = form.email.value;
    const password = form.password.value;

    try {
      const result = await api.login(email, password);
      UI.showToast('Login successful!', 'success');
      this.showApp();
    } catch (error) {
      UI.showToast('Login failed. Please check your credentials.', 'error');
    }
  }

  navigateTo(page) {
    this.currentPage = page;
    switch (page) {
      case 'home':
        this.renderHomePage();
        break;
      case 'rides':
        this.renderRidesPage();
        break;
      case 'drivers':
        this.renderDriversPage();
        break;
      case 'favorites':
        this.renderFavoritesPage();
        break;
      case 'messages':
        this.renderMessagesPage();
        break;
      case 'settings':
        this.renderSettingsPage();
        break;
    }
  }

  async renderHomePage() {
    UI.renderPage('home', `
      <div class="card">
        <h2 class="card-title">🚲 Request a Ride</h2>
        <form id="ride-request-form">
          <div class="form-group">
            <label class="form-label">Pickup Location</label>
            <div style="display: flex; gap: 8px;">
              <input type="text" class="form-input" id="pickup-location" readonly placeholder="Getting location...">
              <button type="button" class="btn btn-outline" id="get-location-btn">📍</button>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Destination</label>
            <input type="text" class="form-input" id="dropoff-location" placeholder="Where to?">
          </div>
          <div class="form-group">
            <label class="form-label">Ride Type</label>
            <div style="display: flex; gap: 8px;">
              <button type="button" class="btn btn-primary" data-ride-type="on-demand" style="flex: 1;">
                Now
              </button>
              <button type="button" class="btn btn-outline" data-ride-type="scheduled" style="flex: 1;">
                Schedule
              </button>
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-block">Request Ride</button>
        </form>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Available Drivers</h3>
          <button class="btn btn-text" id="refresh-drivers">🔄</button>
        </div>
        <div id="drivers-list">Loading...</div>
      </div>
    `);

    // Get current location
    try {
      const position = await locationService.getCurrentPosition();
      document.getElementById('pickup-location').value = `${position.lat.toFixed(4)}, ${position.lon.toFixed(4)}`;
    } catch (error) {
      document.getElementById('pickup-location').value = 'Location unavailable';
    }

    // Load available drivers
    this.loadAvailableDrivers();

    // Event listeners
    document.getElementById('get-location-btn').addEventListener('click', async () => {
      try {
        const position = await locationService.getCurrentPosition();
        document.getElementById('pickup-location').value = `${position.lat.toFixed(4)}, ${position.lon.toFixed(4)}`;
        UI.showToast('Location updated', 'success');
      } catch (error) {
        UI.showToast(error.message, 'error');
      }
    });

    document.getElementById('refresh-drivers').addEventListener('click', () => this.loadAvailableDrivers());
  }

  async loadAvailableDrivers() {
    const list = document.getElementById('drivers-list');
    list.innerHTML = '<div class="skeleton" style="height: 100px;"></div>';

    try {
      const result = await api.getAvailableDrivers();
      const drivers = result.drivers || [];

      if (drivers.length === 0) {
        list.innerHTML = '<p style="text-align: center; color: var(--text-muted); padding: 16px;">No drivers available</p>';
        return;
      }

      list.innerHTML = drivers.map(d => {
        const driver = d[0] || d;
        return `
          <div class="driver-card" style="display: flex; align-items: center; gap: 12px; padding: 12px; border-bottom: 1px solid var(--border);">
            <div class="user-avatar">${(driver['driver/name'] || '?')[0]}</div>
            <div style="flex: 1;">
              <div style="font-weight: 600;">${driver['driver/name'] || 'Driver'}</div>
              <div style="font-size: 14px; color: var(--text-muted);">
                ⭐ ${(driver['driver/rating'] || 5).toFixed(1)} · ${driver['driver/total-rides'] || 0} rides
              </div>
            </div>
            <button class="btn btn-primary btn-sm" onclick="app.requestSpecificDriver('${driver['crux.db/id']}')">
              Request
            </button>
          </div>
        `;
      }).join('');
    } catch (error) {
      list.innerHTML = '<p style="color: var(--danger);">Failed to load drivers</p>';
    }
  }

  async renderRidesPage() {
    UI.renderPage('rides', `
      <div class="card">
        <h2 class="card-title">My Rides</h2>
        <div id="rides-list">Loading...</div>
      </div>
    `);

    try {
      const result = await api.getMyRides();
      const rides = result.rides || [];
      const list = document.getElementById('rides-list');

      if (rides.length === 0) {
        list.innerHTML = '<p style="text-align: center; padding: 32px;">No rides yet</p>';
        return;
      }

      list.innerHTML = rides.map(r => {
        const ride = r[0] || r;
        return `
          <div class="ride-card" style="padding: 16px; border-bottom: 1px solid var(--border);">
            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
              <span style="font-weight: 600;">${ride['ride/ride-type'] || 'On-Demand'}</span>
              <span class="badge">${ride['ride/status']}</span>
            </div>
            <div style="font-size: 14px; color: var(--text-muted);">
              ${UI.formatDateTime(ride['ride/created-at'])}
            </div>
          </div>
        `;
      }).join('');
    } catch (error) {
      document.getElementById('rides-list').innerHTML = '<p>Error loading rides</p>';
    }
  }

  async renderDriversPage() {
    UI.renderPage('drivers', '<div class="card"><h2>Drivers</h2><p>Coming soon...</p></div>');
  }

  async renderFavoritesPage() {
    UI.renderPage('favorites', '<div class="card"><h2>Favorite Drivers</h2><p>Coming soon...</p></div>');
  }

  async renderMessagesPage() {
    UI.renderPage('messages', '<div class="card"><h2>Messages</h2><p>Coming soon...</p></div>');
  }

  async renderSettingsPage() {
    UI.renderPage('settings', `
      <div class="card">
        <h2 class="card-title">Settings</h2>
        <div class="form-group">
          <label class="form-label">Notifications</label>
          <button id="enable-notifications" class="btn btn-outline btn-block">Enable Push Notifications</button>
        </div>
        <div class="form-group">
          <label class="form-label">Location</label>
          <button id="test-location" class="btn btn-outline btn-block">Test Location</button>
        </div>
      </div>
    `);

    document.getElementById('enable-notifications')?.addEventListener('click', () => this.requestNotificationPermission());
    document.getElementById('test-location')?.addEventListener('click', async () => {
      try {
        const pos = await locationService.getCurrentPosition();
        UI.showToast(`Lat: ${pos.lat}, Lon: ${pos.lon}`, 'success', 5000);
      } catch (error) {
        UI.showToast(error.message, 'error');
      }
    });
  }

  async requestNotificationPermission() {
    if (!('Notification' in window)) {
      return;
    }

    if (Notification.permission === 'granted') {
      UI.showToast('Notifications already enabled', 'success');
      return;
    }

    const permission = await Notification.requestPermission();
    if (permission === 'granted') {
      UI.showToast('Notifications enabled!', 'success');
    }
  }

  showInstallPrompt() {
    document.getElementById('install-prompt').classList.remove('hidden');

    document.getElementById('install-btn').addEventListener('click', async () => {
      if (!this.deferredPrompt) return;

      this.deferredPrompt.prompt();
      const { outcome } = await this.deferredPrompt.userChoice;
      this.deferredPrompt = null;
      document.getElementById('install-prompt').classList.add('hidden');
    });

    document.getElementById('install-dismiss').addEventListener('click', () => {
      document.getElementById('install-prompt').classList.add('hidden');
    });
  }

  updateOnlineStatus(isOnline) {
    const indicator = document.getElementById('offline-indicator');
    if (isOnline) {
      indicator.classList.add('hidden');
      UI.showToast('Back online', 'success');
    } else {
      indicator.classList.remove('hidden');
    }
  }

  logout() {
    api.logout();
    window.location.reload();
  }
}

// Initialize app
const app = new SuchCabApp();
