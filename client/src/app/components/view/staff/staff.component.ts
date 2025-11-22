import {Component, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {UserRepository} from '../../../repositories/user.repository';
import {PrimeTemplate} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {AuthService} from '../../../services/auth.service';
import {Role} from '../../../models/auth/role.model';
import {Button} from 'primeng/button';
import {NgIf} from '@angular/common';
import {FloatLabel} from 'primeng/floatlabel';
import {InputText} from 'primeng/inputtext';
import {Password} from 'primeng/password';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RegisterReq} from '../../../models/auth/register.model';
import {AuthRepository} from '../../../repositories/auth.repository';
import {Select} from 'primeng/select';
import {Dialog} from 'primeng/dialog';
import {User} from '../../../models/user/user.model';

@Component({
  selector: 'staff-view',
  standalone: true,
  templateUrl: './staff.component.html',
  imports: [
    NavHeaderComponent,
    PrimeTemplate,
    TableModule,
    Button,
    NgIf,
    FloatLabel,
    InputText,
    Password,
    ReactiveFormsModule,
    FormsModule,
    Select,
    Dialog
  ]
})
export class StaffComponent implements OnInit {
  roleOptions = [
    {
      label: 'Guard',
      value: Role.GUARD
    },
    {
      label: 'Manager',
      value: Role.MANAGER
    }
  ];

  registerDialog = false;

  staff: User[] = [];
  form: RegisterReq = {
    login: '',
    password: '',
    name: '',
    surname: '',
    role: Role.GUARD
  }

  constructor(
    private userRepository: UserRepository,
    private authRepository: AuthRepository,
    protected authService: AuthService
  ) {}

  ngOnInit() {
    this.loadStaff();
  }

  loadStaff() {
    this.userRepository.getStaff().subscribe({
      next: (res) => this.staff = res,
    });
  }

  fire(staff: User) {
    this.userRepository.fire(staff.login).subscribe({
      next: () => this.loadStaff()
    });
  }

  register(): void {
    this.authRepository.registerOther(this.form).subscribe({
      next: () => this.loadStaff()
    });
    this.closeDialog();
  }

  showDialog(): void {
    this.registerDialog = true;
  }

  closeDialog(): void {
    this.registerDialog = false;
    this.resetForm();
  }

  resetForm(): void {
    this.form = {
      login: '',
      password: '',
      name: '',
      surname: '',
      role: Role.GUARD
    };
  }

  protected readonly Role = Role;
}
