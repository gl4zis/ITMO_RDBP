import {Component, OnDestroy, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {Button} from 'primeng/button';
import {NgForOf, NgIf} from '@angular/common';
import {Notification} from '../../../models/notification/notification.model';
import {NotificationRepository} from '../../../repositories/notification.repository';
import {Card} from 'primeng/card';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'notifications-view',
  standalone: true,
  imports: [
    NavHeaderComponent,
    Button,
    NgForOf,
    NgIf,
    Card,
    RouterLink
  ],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];

  private refreshInterval: any;

  constructor(
    private notificationRepository: NotificationRepository
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.refreshInterval = setInterval(() => this.loadNotifications(), 60000);
  }

  ngOnDestroy(): void {
    clearInterval(this.refreshInterval);
  }

  loadNotifications(): void {
    this.notificationRepository.getUnreadNotifications().subscribe({
      next: (resp) => this.notifications = resp
    });
  }

  markAllAsRead(): void {
    this.notificationRepository.markAllAsRead().subscribe({
      next: () => this.notifications = []
    });
  }

  markAsRead(id: number): void {
    this.notificationRepository.markAsRead(id).subscribe({
      next: () => this.notifications = this.notifications.filter(notification => notification.id !== id)
    });
  }
}
