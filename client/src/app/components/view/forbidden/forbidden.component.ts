import { Component } from '@angular/core';
import {Router} from '@angular/router';
import {Button} from 'primeng/button';

@Component({
  selector: 'forbidden-view',
  standalone: true,
  imports: [
    Button
  ],
  templateUrl: './forbidden.component.html',
})
export class ForbiddenComponent {
  constructor(protected router: Router) {}
}
