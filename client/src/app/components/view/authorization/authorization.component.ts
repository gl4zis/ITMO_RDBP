import {Component, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {Card} from 'primeng/card';
import {RegisterReq} from '../../../models/auth/register.model';
import {PrimeTemplate} from 'primeng/api';
import {NgIf} from '@angular/common';
import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {FormsModule} from '@angular/forms';
import {Password} from 'primeng/password';
import {FloatLabelModule} from 'primeng/floatlabel';
import {AuthService} from '../../../services/auth.service';
import {Profile} from '../../../models/auth/profile.model';
import {AuthRepository} from '../../../repositories/auth.repository';
import {Role} from '../../../models/auth/role.model';
import {Dialog} from 'primeng/dialog';
import {PasswordChangeReq} from '../../../models/auth/password-change.request';
import {ToastService} from '../../../services/toast.service';

@Component({
  selector: 'authorization-view',
  standalone: true,
  imports: [
    NavHeaderComponent,
    Card,
    PrimeTemplate,
    NgIf,
    Button,
    InputText,
    FormsModule,
    FloatLabelModule,
    Password,
    Dialog
  ],
  templateUrl: './authorization.component.html'
})
export class AuthorizationComponent implements OnInit {
  isSignIn = true;
  form: RegisterReq = {
    login: '',
    password: '',
    name: '',
    surname: '',
    role: Role.NON_RESIDENT
  };

  profile?: Profile;

  changePasswordDialog = false;
  cpForm: PasswordChangeReq = {
    oldPassword: "",
    newPassword: ""
  };

  constructor(
    protected authService: AuthService,
    private authRepository: AuthRepository,
    private toast: ToastService,
  ) {}

  ngOnInit() {
    this.authService.isAuthorized$.subscribe(isAuthorized => {
      if (isAuthorized) {
        this.authRepository.getProfile().subscribe({
          next: (resp) => this.profile = resp
        });
      } else {
        this.profile = undefined;
      }
    });
  }

  onSubmit(): void {
    if (this.isSignIn) {
      this.authService.login(this.form);
    } else {
      this.authService.register(this.form);
    }
    this.resetForm();
  }

  changeForm(): void {
    this.isSignIn = !this.isSignIn;
    this.resetForm();
  }

  getTitle(): string {
    if (this.authService.isAuthorized()) {
      return 'Profile';
    }
    return this.isSignIn ? 'Sign In' : 'Sign Up';
  }

  changePassword(): void {
    this.authRepository.changePassword(this.cpForm).subscribe({
      next: () => {
        this.closePasswordChanger();
        this.authService.logout();
        this.toast.success("Password was changed", "Authorize, using new password");
      }
    });
  }

  openPasswordChanger(): void {
    this.changePasswordDialog = true;
  }

  closePasswordChanger(): void {
    this.changePasswordDialog = false;
    this.cpForm = {
      oldPassword: "",
      newPassword: ""
    };
  }

  private resetForm(): void {
    this.form = {
      login: '',
      password: '',
      name: '',
      surname: '',
      role: Role.NON_RESIDENT
    };
  }

  protected readonly Role = Role;
}
