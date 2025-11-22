import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError, Observable} from 'rxjs';
import {ToastService} from '../services/toast.service';
import {StorageService} from '../services/storage.service';
import {AuthService} from '../services/auth.service';

@Injectable()
export class RequestInterceptor implements HttpInterceptor {
  constructor(
    private storage: StorageService,
    private authService: AuthService,
    private toast: ToastService
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.storage.getToken();
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        switch (error.status) {
          case 0: {
            this.toast.error('X0', 'No connection with server')
            break;
          }
          case 400: {
            this.toast.warn('Oops', 'Looks like you tried to send some wrong data');
            break;
          }
          case 401: {
            this.toast.warn('Oops', 'Looks like you are unauthorized. Try to sign in');
            this.authService.updateAuthState();
            break;
          }
          case 403: {
            this.toast.warn('Oops', 'Looks like you don\'t have permissions for this resource');
            this.authService.updateAuthState();
            break;
          }
          case 404: {
            this.toast.warn('Oops', 'Looks like server can\'t find item from your request');
            break;
          }
          default: this.toast.httpError(error);
        }

        throw error;
      })
    );
  }
}
