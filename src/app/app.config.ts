import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';

// Backend API endpoint used by the frontend to send contact requests.
// Change this if you host the backend on a different origin.
export const contactApiUrl = '/api/contact';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
  ]
};
