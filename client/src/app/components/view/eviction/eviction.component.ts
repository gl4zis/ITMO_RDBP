import {Component, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {BidCardComponent} from '../bids/bid-card/bid-card.component';
import {NgForOf, NgIf} from '@angular/common';
import {UserRepository} from '../../../repositories/user.repository';
import {Eviction, EVICTION_REASON_LOCALIZATION_MAP} from '../../../models/eviction.model';
import {Card} from 'primeng/card';
import {Role} from '../../../models/auth/role.model';
import {RouterLink} from '@angular/router';
import {Button} from 'primeng/button';

@Component({
  selector: 'eviction-view',
  standalone: true,
  templateUrl: './eviction.component.html',
  imports: [
    NavHeaderComponent,
    BidCardComponent,
    NgForOf,
    Card,
    NgIf,
    RouterLink,
    Button
  ]
})
export class EvictionComponent implements OnInit {
  evictions: Eviction[] = [];

  constructor(
    private userRepo: UserRepository
  ) {}

  ngOnInit() {
    this.loadEvictions();
  }

  loadEvictions() {
    this.userRepo.getEvictions().subscribe({
      next: (res) => this.evictions = res
    });
  }

  evict(eviction: Eviction) {
    this.userRepo.evict(eviction.resident.login).subscribe({
      next: () => this.loadEvictions()
    });
  }

  protected readonly Role = Role;
  protected readonly EVICTION_REASON_LOCALIZATION_MAP = EVICTION_REASON_LOCALIZATION_MAP;
}
