import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router
} from '@angular/router';
import {Injectable} from '@angular/core';
import {Role} from '../models/auth/role.model';
import {Utils} from '../services/utils';
import {AuthService} from "../services/auth.service";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    if (!this.authService.isAuthorized()) {
      this.router.navigate(['auth']);
      return false;
    }

    const availableRoles: Role[] = route.data['roles'];
    if (Utils.isUndefined(availableRoles) ||
        availableRoles.some((role: Role) => this.authService.getRole() == role)
    ) {
      return true;
    }

    this.router.navigate(['forbidden']);
    return false;
  }
}
