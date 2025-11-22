enum Protocol {
  HTTP = 'http',
  HTTPS = 'https',
}

const API_PROTOCOL = Protocol.HTTP;
const SERVER_HOST = 'localhost';
const SERVER_PORT = 8183;

function createApiUrl(): string {
  return `${API_PROTOCOL}://${SERVER_HOST}:${SERVER_PORT}`;
}

export const environment = {
  api: createApiUrl()
};
