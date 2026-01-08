import {Injectable} from "@angular/core";
import {BehaviorSubject} from 'rxjs';
import {StorageService} from './storage.service';
import {AuthRepository} from '../repositories/auth.repository';
import {ToastService} from './toast.service';
import {Utils} from './utils';
import {jwtDecode} from 'jwt-decode';
import {LoginReq} from '../models/auth/login.model';
import {Role} from '../models/auth/role.model';
import {RegisterReq} from '../models/auth/register.model';
import {AuthTokens} from '../models/auth/auth-tokens.model';
import {Router} from '@angular/router';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    isAuthorized$ = new BehaviorSubject<boolean>(false);

    constructor(
        private storage: StorageService,
        private authRepo: AuthRepository,
        private toastService: ToastService,
        private router: Router
    ) {
        this.updateAuthState();
        setInterval(() => {
            this.refreshTokenIfNeeded();
            this.updateAuthState();
        }, 60000);
    }

    isAuthorized(): boolean {
        const token = this.storage.getAccessToken();
        if (Utils.isUndefined(token)) {
            return false;
        }

        try {
            const payload: any = jwtDecode(token!);
            const now = Math.floor(Date.now() / 1000);
            if (Utils.isUndefined(payload.exp) ||
                Utils.isUndefined(payload.sub) ||
                Utils.isUndefined(payload.role)
            ) {
                console.log("Invalid JWT token");
                return false;
            }
            return payload.exp! > now;
        } catch (error) {
            console.error(error);
            return false;
        }
    }

    getRole(): Role | undefined {
        // DON'T remove this check before next unsafe logic
        if (!this.isAuthorized()) {
            return undefined;
        }

        const payload: any = jwtDecode(this.storage.getAccessToken()!);
        return payload.role;
    }

    getLogin(): string | undefined {
        // DON'T remove this check before next unsafe logic
        if (!this.isAuthorized()) {
            return undefined;
        }

        const payload: any = jwtDecode(this.storage.getAccessToken()!);
        return payload.sub;
    }

    logout(): void {
        this.storage.resetAuth();
        this.updateAuthState();
    }

    private shouldRefreshToken(): boolean {
        const token = this.storage.getAccessToken();
        if (Utils.isUndefined(token)) {
            return false;
        }

        try {
            const payload: any = jwtDecode(token!);
            const now = Math.floor(Date.now() / 1000);
            const timeToExpiry = payload.exp - now;

            // Refresh if token expires in less than 5 minutes
            return timeToExpiry < 300;
        } catch (error) {
            console.error('Error checking token expiry:', error);
            return false;
        }
    }

    private refreshTokenIfNeeded(): void {
        if (this.shouldRefreshToken()) {
            const refreshToken = this.storage.getRefreshToken();
            if (refreshToken) {
                this.authRepo.refresh(refreshToken).subscribe({
                    next: (tokens: AuthTokens) => {
                        this.storage.saveTokens(tokens.accessToken, tokens.refreshToken);
                        console.log('Tokens refreshed successfully');
                    },
                    error: (error) => {
                        console.error('Failed to refresh token:', error);
                        this.logout();
                    }
                });
            }
        }
    }

    login(req: LoginReq): void {
        this.authRepo.login(req).subscribe({
            next: (resp: AuthTokens) => {
                this.storage.saveTokens(resp.accessToken, resp.refreshToken);
                this.updateAuthState();
            },
            error: () => this.logout()
        });
    }

    register(req: RegisterReq): void {
        this.authRepo.register(req).subscribe({
            next: (resp: AuthTokens) => {
                this.storage.saveTokens(resp.accessToken, resp.refreshToken);
                this.updateAuthState();
            },
            error: () => this.logout()
        });
    }

    updateAuthState(): void {
        const authState = this.isAuthorized();
        if (this.isAuthorized$.value != authState) {
            this.isAuthorized$.next(authState);
        }

        if (!authState) {
          this.storage.resetAuth();
          this.router.navigate(['auth']);
        }
    }
}
