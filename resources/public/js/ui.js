// UI utilities and components
class UI {
  // Toast notifications
  static showToast(message, type = 'info', duration = 3000) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => container.removeChild(toast), 300);
    }, duration);
  }

  // Loading spinner
  static showLoading(element) {
    const spinner = document.createElement('div');
    spinner.className = 'spinner';
    spinner.id = 'loading-spinner';
    element.appendChild(spinner);
  }

  static hideLoading() {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) spinner.remove();
  }

  // Modal
  static showModal(title, content, actions = []) {
    const container = document.getElementById('modal-container');
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
      <div class="modal-backdrop" onclick="UI.closeModal()"></div>
      <div class="modal-content">
        <div class="modal-header">
          <h3>${title}</h3>
          <button onclick="UI.closeModal()" class="icon-btn">×</button>
        </div>
        <div class="modal-body">${content}</div>
        <div class="modal-footer">
          ${actions.map(action => `
            <button class="btn ${action.class}" onclick="${action.onclick}">
              ${action.label}
            </button>
          `).join('')}
        </div>
      </div>
    `;
    container.appendChild(modal);
  }

  static closeModal() {
    const container = document.getElementById('modal-container');
    container.innerHTML = '';
  }

  // Menu toggle
  static toggleMenu() {
    const menu = document.getElementById('side-menu');
    menu.classList.toggle('open');
  }

  static closeMenu() {
    const menu = document.getElementById('side-menu');
    menu.classList.remove('open');
  }

  // Page rendering
  static renderPage(pageName, content) {
    const mainContent = document.getElementById('main-content');
    mainContent.innerHTML = content;

    // Update navigation active states
    document.querySelectorAll('.menu-item, .nav-item').forEach(item => {
      if (item.dataset.page === pageName) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });

    UI.closeMenu();
  }

  // Format date/time
  static formatDateTime(isoString) {
    const date = new Date(isoString);
    return date.toLocaleString();
  }

  static formatTime(isoString) {
    const date = new Date(isoString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  // Avatar
  static getAvatarHTML(user) {
    if (user.avatar_url || user['avatar-url']) {
      return `<img src="${user.avatar_url || user['avatar-url']}" alt="${user.name}">`;
    }
    return `<div class="user-avatar">${(user.name || user.username || '?')[0].toUpperCase()}</div>`;
  }
}

// Add CSS for modal
const modalStyles = `
<style>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: relative;
  background: var(--white);
  border-radius: var(--radius);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border);
}

.modal-body {
  padding: 16px;
}

.modal-footer {
  padding: 16px;
  border-top: 1px solid var(--border);
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
`;
document.head.insertAdjacentHTML('beforeend', modalStyles);
