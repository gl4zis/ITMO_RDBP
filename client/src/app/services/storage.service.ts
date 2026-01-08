import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';

  resetAuth(): void {
    sessionStorage.removeItem(this.ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }

  saveTokens(accessToken: string, refreshToken: string): void {
    sessionStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
    sessionStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  getAccessToken(): string | undefined {
    return this.getItem(this.ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | undefined {
    return this.getItem(this.REFRESH_TOKEN_KEY);
  }

  private getItem(name: string): string | undefined {
    const item = sessionStorage.getItem(name);
    return item ? item : undefined;
  }
}
