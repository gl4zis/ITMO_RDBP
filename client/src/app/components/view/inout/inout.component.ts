import {Component, OnInit} from '@angular/core';
import {GuardHistory} from '../../../models/guard-history.model';
import {GuardRepository} from '../../../repositories/guard.repository';
import {AuthService} from '../../../services/auth.service';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {InoutGuardComponent} from './guard/inout-guard.component';
import {Role} from '../../../models/auth/role.model';
import {NgIf} from '@angular/common';
import {TableModule} from 'primeng/table';
import {Button} from 'primeng/button';
import {Card} from 'primeng/card';
import {FloatLabel} from 'primeng/floatlabel';
import {InputText} from 'primeng/inputtext';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Utils} from '../../../services/utils';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'inout-view',
  standalone: true,
  imports: [
    NavHeaderComponent,
    InoutGuardComponent,
    NgIf,
    TableModule,
    Button,
    Card,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './inout.component.html'
})
export class InoutComponent implements OnInit {
  searchResident: string = "";
  history: GuardHistory[] = [];

  constructor(
    private guardRepository: GuardRepository,
    private router: Router,
    private route: ActivatedRoute,
    protected authService: AuthService,
  ) {}

  ngOnInit() {
    if (this.authService.getRole() === Role.MANAGER) {
      this.route.queryParams.subscribe(params => {
        if (params['resident']) {
          this.searchResident = params['resident'];
          this.loadHistory();
        } else {
          this.searchResident = "";
        }
      });
    } else if (this.authService.getRole() === Role.RESIDENT) {
      this.loadSelfHistory();
    }
  }

  findResidentHistory(): void {
    if (this.searchResident && this.searchResident.length) {
      if (this.searchResident === this.route.snapshot.queryParamMap.get('resident')) {
        this.loadHistory();
      }
      Utils.changeQueryParam(this.route, this.router, { resident: this.searchResident });
    }
  }

  loadHistory() {
    this.guardRepository.getHistory(this.searchResident).subscribe({
      next: (resp) => this.history = resp
    });
  }

  loadSelfHistory() {
    this.guardRepository.getSelfHistory().subscribe({
      next: (resp) => this.history = resp
    });
  }

  protected readonly Role = Role;
}
