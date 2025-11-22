import {Injectable} from '@angular/core';
import {MessageService} from 'primeng/api';
import {HttpErrorResponse} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  constructor(private service: MessageService) {}

  httpError(error: HttpErrorResponse): void {
    if (this.is4xx(error.status)) {
      this.warn(error.statusText, error.error);
    } else {
      this.error('XXX', 'Something went wrong');
      console.error(error);
    }
  }

  success(summary: string, detail?: string): void {
    this.message('success', summary, detail);
  }

  info(summary: string, detail?: string): void {
    this.message('info', summary, detail);
  }

  warn(summary: string, detail?: string): void {
    this.message('warn', summary, detail);
  }

  error(summary: string, detail?: string): void {
    this.message('error', summary, detail);
  }

  private message(severity: string, summary: string, detail?: string) {
    this.service.add({
      severity: severity,
      summary: summary,
      detail: detail
    });
  }

  private is4xx(status: number) {
    return status >= 400 && status <= 499;
  }
}
