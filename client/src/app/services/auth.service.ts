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
import {OneFieldModel} from '../models/one-field.model';
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
        setInterval(() => this.updateAuthState(), 60000);
    }

    isAuthorized(): boolean {
        const token = this.storage.getToken();
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

        const payload: any = jwtDecode(this.storage.getToken()!);
        return payload.role;
    }

    getLogin(): string | undefined {
        // DON'T remove this check before next unsafe logic
        if (!this.isAuthorized()) {
            return undefined;
        }

        const payload: any = jwtDecode(this.storage.getToken()!);
        return payload.sub;
    }

    logout(): void {
        this.storage.resetAuth();
        this.updateAuthState();
    }

    login(req: LoginReq): void {
        this.authRepo.login(req).subscribe({
            next: (resp: OneFieldModel<string>) => {
                this.storage.saveToken(resp.data);
                this.updateAuthState();
            },
            error: () => this.logout()
        });
    }

    register(req: RegisterReq): void {
        this.authRepo.register(req).subscribe({
            next: (resp: OneFieldModel<string>) => {
                this.storage.saveToken(resp.data)
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
