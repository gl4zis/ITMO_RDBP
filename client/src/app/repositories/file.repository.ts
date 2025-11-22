import {Injectable} from '@angular/core';
import {environment} from '../environment/environment';

@Injectable({
  providedIn: 'root'
})
export class FileRepository {
  private api = `${environment.api}/file`;

  constructor() {}

  createDownloadLink(key: string): string {
    return `${this.api}/download/${key}`;
  }

  uploadLink(): string {
    return `${this.api}/upload`;
  }
}
