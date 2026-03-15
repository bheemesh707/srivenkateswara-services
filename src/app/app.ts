import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { contactApiUrl } from './app.config';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  showAbout = false;
  adminOpen = false;
  adminLoggedIn = false;
  adminToken = '';
  adminError = '';
  adminSuccess = '';

  adminRequests: Array<{
    timestamp: string;
    fullName: string;
    email: string;
    loanType: string;
    amount: string;
  }> = [];

  // Fields for changing credentials
  adminNewUsername = '';
  adminNewPassword = '';
  adminConfirmPassword = '';

  toggleAbout(event: Event) {
    event.preventDefault();

    // Ensure the About section is shown, then scroll it into view.
    this.showAbout = true;

    // Wait for the template to update before scrolling.
    setTimeout(() => {
      document.querySelector('.about')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  }

  toggleAdmin(event: Event) {
    event.preventDefault();

    // Ensure the admin panel is visible and scroll it into view.
    this.adminOpen = true;

    setTimeout(() => {
      document.querySelector('.admin')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  }

  async loginAdmin(username: string, password: string) {
    if (!username || !password) {
      this.adminError = 'Username and password are required.';
      return;
    }

    const encoded = btoa(`${username}:${password}`);
    this.adminError = '';

    try {
      const res = await fetch('/api/admin/requests/list', {
        method: 'GET',
        headers: { Authorization: `Basic ${encoded}` },
      });

      if (!res.ok) {
        throw new Error(`Status ${res.status}`);
      }

      this.adminRequests = await res.json();
      this.adminLoggedIn = true;
      this.adminToken = encoded;
      this.adminSuccess = '';
    } catch (err) {
      this.adminLoggedIn = false;
      this.adminError = 'Invalid credentials or failed to retrieve requests.';
      console.error(err);
    }
  }

  async changeAdminCredentials() {
    if (!this.adminNewUsername || !this.adminNewPassword) {
      this.adminError = 'New username and password are required.';
      return;
    }

    if (this.adminNewPassword !== this.adminConfirmPassword) {
      this.adminError = 'Password and confirmation do not match.';
      return;
    }

    this.adminError = '';
    this.adminSuccess = '';

    try {
      const res = await fetch('/api/admin/credentials', {
        method: 'POST',
        headers: {
          Authorization: `Basic ${this.adminToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: this.adminNewUsername,
          password: this.adminNewPassword,
        }),
      });

      if (!res.ok) {
        throw new Error(`Status ${res.status}`);
      }

      // Update stored token so further calls use the new credentials
      this.adminToken = btoa(`${this.adminNewUsername}:${this.adminNewPassword}`);
      this.adminSuccess = 'Credentials updated successfully.';
      this.adminNewUsername = '';
      this.adminNewPassword = '';
      this.adminConfirmPassword = '';
    } catch (err) {
      this.adminError = 'Failed to update credentials. Please try again.';
      console.error(err);
    }
  }

  logoutAdmin() {
    this.adminLoggedIn = false;
    this.adminToken = '';
    this.adminRequests = [];
    this.adminError = '';
  }

  async sendRequest(
    event: Event,
    fullName: string,
    email: string,
    loanType: string,
    amount: string
  ) {
    event.preventDefault();

    const payload = {
      fullName,
      email,
      loanType,
      amount,
    };

    try {
      const res = await fetch(contactApiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        throw new Error(`Server returned ${res.status}`);
      }

      alert('Request sent! We will reach out soon.');
    } catch (error) {
      console.error(error);
      alert('Something went wrong sending your request. Please try again later.');
    }
  }
}
