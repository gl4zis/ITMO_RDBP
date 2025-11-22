import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  private readonly TOKEN_KEY = 'jwt';

  resetAuth(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
  }

  saveToken(token: string): void {
    sessionStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | undefined {
    return this.getItem(this.TOKEN_KEY);
  }

  private getItem(name: string): string | undefined {
    const item = sessionStorage.getItem(name);
    return item ? item : undefined;
  }
}
