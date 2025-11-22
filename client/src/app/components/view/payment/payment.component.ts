import {Component, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {AuthService} from '../../../services/auth.service';
import {Role} from '../../../models/auth/role.model';
import {PrimeTemplate} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {NgIf} from '@angular/common';
import {Button} from 'primeng/button';
import {Card} from 'primeng/card';
import {FloatLabel} from 'primeng/floatlabel';
import {InputText} from 'primeng/inputtext';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Utils} from '../../../services/utils';
import {ActivatedRoute, Router} from '@angular/router';
import {PaymentRepository} from '../../../repositories/payment.repository';
import {PaymentInfo} from '../../../models/payment/payment-info.model';
import {InputMask} from 'primeng/inputmask';
import {Password} from 'primeng/password';

@Component({
  selector: 'payment-view',
  standalone: true,
  templateUrl: './payment.component.html',
  imports: [
    NavHeaderComponent,
    PrimeTemplate,
    TableModule,
    NgIf,
    Button,
    Card,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    FormsModule,
    InputMask,
    Password
  ]
})
export class PaymentComponent implements OnInit {
  searchResident: string = "";
  paymentInfo?: PaymentInfo;

  constructor(
    protected authService: AuthService,
    private paymentRepository: PaymentRepository,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    if (this.authService.getRole() === Role.MANAGER) {
      this.route.queryParams.subscribe(params => {
        if (params['resident']) {
          this.searchResident = params['resident'];
          this.loadData();
        } else {
          this.searchResident = "";
        }
      });
    } else {
      this.loadSelfData();
    }
  }

  findResidentHistory(): void {
    if (this.searchResident && this.searchResident.length) {
      if (this.searchResident === this.route.snapshot.queryParamMap.get('resident')) {
        this.loadData();
      } else {
        Utils.changeQueryParam(this.route, this.router, {resident: this.searchResident});
      }
    }
  }

  loadData(): void {
    this.paymentRepository.getPaymentInfo(this.searchResident).subscribe({
      next: (resp) => {
        this.paymentInfo = resp;
        this.paymentInfo.history = this.paymentInfo.history.map(h => ({...h, timestamp: new Date(h.timestamp)}));
      }
    });

  }

  loadSelfData(): void {
    this.paymentRepository.getSelfPaymentInfo().subscribe({
      next: (resp) => {
        this.paymentInfo = resp;
        this.paymentInfo.history = this.paymentInfo.history.map(h => ({...h, timestamp: new Date(h.timestamp)}));
      }
    });
  }

  pay() {
    if (this.paymentInfo && this.paymentInfo.debt) {
      this.paymentRepository.pay({sum: this.paymentInfo.debt}).subscribe({
        next: () => this.loadSelfData()
      });
    }
  }

  protected readonly Role = Role;
  protected readonly Utils = Utils;
}
