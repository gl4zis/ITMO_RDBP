import {Component, OnInit} from '@angular/core';
import {MenubarModule} from 'primeng/menubar';
import {MenuItem} from 'primeng/api';
import {ProfileButtonComponent} from '../profile-button/profile-button.component';
import {Role} from '../../../models/auth/role.model';
import {Router} from '@angular/router';
import {AuthService} from '../../../services/auth.service';

@Component({
  selector: 'nav-header',
  standalone: true,
  imports: [
    MenubarModule,
    ProfileButtonComponent
  ],
  templateUrl: './nav-header.component.html'
})
export class NavHeaderComponent implements OnInit {
  private readonly basePages: MenuItem[] = [
    {
      label: 'Notifications',
      command: () => this.router.navigate(['notifications']),
      icon: 'pi pi-envelope'
    }
  ];

  private readonly nonResidentPages: MenuItem[] = [
    {
      label: 'Bids',
      command: () => this.router.navigate(['bids']),
      icon: 'pi pi-file-edit'
    },
  ];

  private readonly residentPages: MenuItem[] = [
    {
      label: 'In/Out',
      command: () => this.router.navigate(['inout']),
      icon: 'pi pi-arrow-right-arrow-left'
    },
    {
      label: 'Bids',
      command: () => this.router.navigate(['bids']),
      icon: 'pi pi-file-edit'
    },
    {
      label: 'Payment',
      command: () => this.router.navigate(['payment']),
      icon: 'pi pi-check-circle'
    }
  ];

  private readonly guardPages: MenuItem[] = [
    {
      label: 'In/Out',
      command: () => this.router.navigate(['inout']),
      icon: 'pi pi-arrow-right-arrow-left'
    }
  ];

  private readonly managerPages: MenuItem[] = [
    {
      label: 'In/Out',
      command: () => this.router.navigate(['inout']),
      icon: 'pi pi-arrow-right-arrow-left'
    },
    {
      label: 'Bids',
      command: () => this.router.navigate(['bids']),
      icon: 'pi pi-file-edit'
    },
    {
      label: 'Payment',
      command: () => this.router.navigate(['payment']),
      icon: 'pi pi-check-circle'
    },
    {
      label: 'Staff',
      command: () => this.router.navigate(['staff']),
      icon: 'pi pi-address-book'
    },
    {
      label: 'Residents',
      command: () => this.router.navigate(['residents']),
      icon: 'pi pi-user'
    },
    {
      label: 'Evictions',
      command: () => this.router.navigate(['eviction']),
      icon: 'pi pi-backward'
    },
    {
      label: 'Buildings',
      icon: 'pi pi-building',
      items: [
        {
          label: 'University',
          command: () => this.router.navigate(['university']),
          icon: 'pi pi-building-columns'
        },
        {
          label: 'Dormitory',
          command: () => this.router.navigate(['dormitory']),
          icon: 'pi pi-home'
        },
        {
          label: 'Room',
          command: () => this.router.navigate(['room']),
          icon: 'pi pi-box'
        }
      ]
    }
  ];

  pages: MenuItem[] = [];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.updateTabs();
    this.authService.isAuthorized$.subscribe(() => this.updateTabs());
  }

  private updateTabs() {
    this.pages = this.basePages;
    const userRole = this.authService.getRole();
    switch (userRole) {
      case Role.NON_RESIDENT:
        this.pages = this.pages.concat(this.nonResidentPages);
        break;
      case Role.RESIDENT:
        this.pages = this.pages.concat(this.residentPages);
        break;
      case Role.GUARD:
        this.pages = this.pages.concat(this.guardPages);
        break;
      case Role.MANAGER:
        this.pages = this.pages.concat(this.managerPages);
        break;
      default:
        this.pages = [];
    }
  }
}
