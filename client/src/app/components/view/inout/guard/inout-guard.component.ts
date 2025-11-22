import {Component} from '@angular/core';
import {GuardRepository} from '../../../../repositories/guard.repository';
import {ToastService} from '../../../../services/toast.service';
import {Button} from 'primeng/button';
import {FormsModule} from '@angular/forms';
import {InputText} from 'primeng/inputtext';
import {Card} from 'primeng/card';
import {PrimeTemplate} from 'primeng/api';
import {FloatLabelModule} from 'primeng/floatlabel';

@Component({
  selector: 'inout-guard-page',
  standalone: true,
  templateUrl: './inout-guard.component.html',
  imports: [
    Button,
    FormsModule,
    InputText,
    Card,
    PrimeTemplate,
    FloatLabelModule
  ]
})
export class InoutGuardComponent {
  login: string = "";

  constructor(
    private guardRepository: GuardRepository,
    private toast: ToastService,
  ) {}

  entry(): void {
    if (this.login && this.login.length) {
      this.guardRepository.entry(this.login).subscribe({
        next: () => this.toast.success("Entry was successfully registered"),
      });
    }
    this.login = "";
  }

  exit(): void {
    if (this.login && this.login.length) {
      this.guardRepository.exit(this.login).subscribe({
        next: () => this.toast.success("Exit was successfully registered"),
      });
    }
    this.login = "";
  }
}
