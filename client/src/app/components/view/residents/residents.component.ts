import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {PrimeTemplate} from 'primeng/api';
import {Table, TableModule} from 'primeng/table';
import {Role} from '../../../models/auth/role.model';
import {Resident} from '../../../models/user/resident.model';
import {UserRepository} from '../../../repositories/user.repository';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Utils} from '../../../services/utils';
import {Button} from 'primeng/button';
import {IconField} from 'primeng/iconfield';
import {InputIcon} from 'primeng/inputicon';
import {InputText} from 'primeng/inputtext';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'residents-view',
  standalone: true,
  templateUrl: './residents.component.html',
  imports: [
    NavHeaderComponent,
    PrimeTemplate,
    TableModule,
    RouterLink,
    Button,
    IconField,
    InputIcon,
    InputText,
    FormsModule
  ]
})
export class ResidentsComponent implements OnInit, AfterViewInit {
  residents: Resident[] = [];

  @ViewChild("table") table!: Table;
  searchValue = '';

  constructor(
    protected userRepository: UserRepository,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.loadResidents();
  }

  ngAfterViewInit() {
    const search = this.route.snapshot.queryParamMap.get('search');
    if (search) {
      this.searchValue = search;
      this.filter();
    }
  }

  loadResidents(): void {
    this.userRepository.getResidents().subscribe({
      next: (resp) => {
        this.residents = resp.map(r => ({...r, lastCameOut: r.lastCameOut && new Date(r.lastCameOut)}));
      }
    });
  }

  filter() {
    this.table.filterGlobal(this.searchValue, 'contains');
    Utils.changeQueryParam(this.route, this.router, { search: this.searchValue });
  }

  protected readonly Role = Role;
  protected readonly Utils = Utils;
}
