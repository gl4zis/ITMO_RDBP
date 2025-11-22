import {AfterViewInit, Component, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../../common/nav-header/nav-header.component';
import {UniversityRepository} from '../../../../repositories/university.repository';
import {University} from '../../../../models/university/university.model';
import {Button} from 'primeng/button';
import {NgForOf, NgIf} from '@angular/common';
import {PrimeTemplate} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {DormitoryRepository} from '../../../../repositories/dormitory.repository';
import {ToastService} from '../../../../services/toast.service';
import {UniversityRequest} from '../../../../models/university/university.request';
import {Dialog} from 'primeng/dialog';
import {FloatLabel} from 'primeng/floatlabel';
import {InputText} from 'primeng/inputtext';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Dormitory} from '../../../../models/dormitory/dormitory.model';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Utils} from '../../../../services/utils';

interface UniversityView extends University {
  dormitories: Dormitory[];
}

@Component({
  selector: 'university-view',
  standalone: true,
  templateUrl: './university.component.html',
  imports: [
    NavHeaderComponent,
    Button,
    NgIf,
    PrimeTemplate,
    TableModule,
    Dialog,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    FormsModule,
    NgForOf,
    RouterLink
  ]
})
export class UniversityComponent implements OnInit {
  universities: UniversityView[] = [];

  viewOpened = false;
  editingForm: UniversityRequest = {
    name: '',
    address: ''
  };
  editingId?: number;

  constructor(
    private universityRepository: UniversityRepository,
    private dormitoryRepository: DormitoryRepository,
    private toast: ToastService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadUniversities();
    const uId = this.route.snapshot.queryParamMap.get('id');
    if (Number(uId)) {
      this.editClick(Number(uId));
    }
  }

  loadUniversities(): void {
    this.universityRepository.getAll().subscribe({
      next: (universities) => {
        this.dormitoryRepository.getAll().subscribe({
          next: (dormitories) => this.universities = universities
            .map(u => ({
              ...u,
              dormitories: dormitories.filter(d => u.dormitoryIds.includes(d.id))
            }))
        });
      }
    });
  }

  editClick(id: number) {
    this.universityRepository.get(id).subscribe({
      next: (university) => this.editingForm = university
    });

    this.editingId = id;
    this.openView();
    Utils.changeQueryParam(this.route, this.router, { id: id });
  }

  deleteClick(u: University) {
    if (u.dormitoryIds.length) {
      this.toast.warn('Delete failed', 'This university connects to dormitories');
    } else {
      this.universityRepository.delete(u.id).subscribe({
        next: () => this.loadUniversities()
      });
    }
  }

  openView() {
    this.viewOpened = true;
  }

  closeView() {
    this.viewOpened = false;
    this.resetForm();
  }

  resetForm() {
    this.editingId = undefined;
    this.editingForm = {
      name: '',
      address: ''
    };
    Utils.changeQueryParam(this.route, this.router, { id: undefined });
  }

  saveView(): void {
    if (!this.editingId) {
      this.universityRepository.add(this.editingForm).subscribe({
        next: () => {
          this.loadUniversities();
          this.closeView();
        }
      });
    } else {
      this.universityRepository.update(this.editingId, this.editingForm).subscribe({
        next: () => {
          this.loadUniversities();
          this.closeView();
        }
      });
    }
  }
}
