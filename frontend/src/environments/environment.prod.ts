// This URL must match the backend's public Render URL. If you name the Render
// backend service something other than "vaglia-backend", update this before
// deploying (Render URLs are https://<service-name>.onrender.com).
export const environment = {
  production: true,
  apiBaseUrl: 'https://vaglia-backend.onrender.com/api'
};
