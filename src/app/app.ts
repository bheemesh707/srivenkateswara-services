import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { contactApiUrl } from './app.config';
import * as XLSX from 'xlsx';


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
    phone: string;
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

  // For now, just check against hardcoded credentials
  if (username === 'admin' && password === 'admin123') {
    this.adminLoggedIn = true;
    this.adminError = '';
    this.adminSuccess = 'Login successful!';

    // When admin logs in, allow download of the saved Excel file
    this.downloadRequestsExcel();
  } else {
    this.adminLoggedIn = false;
    this.adminError = 'Invalid credentials.';
  }
}

// Helper function to download the Excel file
downloadRequestsExcel() {
  // Example: if you stored requests in memory as an array
  const requests = [
    { fullName: 'John Doe', email: 'john@example.com', phone: '123-456-7890', loanType: 'Home', amount: '500000' },
    { fullName: 'Jane Smith', email: 'jane@example.com', phone: '098-765-4321', loanType: 'Car', amount: '300000' }
    // ... add more from your saved state
  ];

  // Convert to worksheet
  const worksheet = XLSX.utils.json_to_sheet(requests);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Requests');

  // Trigger download
  XLSX.writeFile(workbook, 'requests.xlsx');
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
  phone: string,
  loanType: string,
  amount: string
) {
  event.preventDefault();

  const payload = { fullName, email, phone, loanType, amount };

  try {
    // Normally you'd send to backend here
    // const res = await fetch(contactApiUrl, {...});

    // Instead, generate Excel file locally
    const data = [
      ['Full Name', 'Email', 'Phone', 'Loan Type', 'Amount'],
      [payload.fullName, payload.email, payload.phone, payload.loanType, payload.amount]
    ];

    const worksheet = XLSX.utils.aoa_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Requests');

    // Trigger download
    XLSX.writeFile(workbook, 'request.xlsx');

    alert('Request sent successfully, our customer executive will be contacting you.');
  } catch (error) {
    console.error(error);
    alert('Something went wrong saving your request.');
  }
}

// async sendRequest(
//     event: Event,
//     fullName: string,
//     email: string,
//     loanType: string,
//     amount: string
//   ) {
//     event.preventDefault();

//     const payload = {
//       fullName,
//       email,
//       loanType,
//       amount,
//     };

//     try {
//       const res = await fetch(contactApiUrl, {
//         method: 'POST',
//         headers: { 'Content-Type': 'application/json' },
//         body: JSON.stringify(payload),
//       });

//       if (!res.ok) {
//         throw new Error(`Server returned ${res.status}`);
//       }

//       alert('Request sent! We will reach out soon.');
//     } catch (error) {
//       console.e  rror(error);
//       alert('Something went wrong sending your request. Please try again later.');
//     }
//   }
}
