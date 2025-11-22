import {Component, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../../common/nav-header/nav-header.component';
import {Button} from 'primeng/button';
import {Dialog} from 'primeng/dialog';
import {FloatLabel} from 'primeng/floatlabel';
import {InputText} from 'primeng/inputtext';
import {NgForOf, NgIf} from '@angular/common';
import {PrimeTemplate} from 'primeng/api';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {UniversityRepository} from '../../../../repositories/university.repository';
import {DormitoryRepository} from '../../../../repositories/dormitory.repository';
import {ToastService} from '../../../../services/toast.service';
import {University} from '../../../../models/university/university.model';
import {Utils} from '../../../../services/utils';
import {Dormitory} from '../../../../models/dormitory/dormitory.model';
import {DormitoryRequest} from '../../../../models/dormitory/dormitory.request';
import {MultiSelect} from 'primeng/multiselect';
import {Selectable} from '../../../../models/selectable.model';

interface DormitoryView extends Dormitory {
  universities: University[];
}

@Component({
  selector: 'dormitory-view',
  standalone: true,
  templateUrl: './dormitory.component.html',
  imports: [
    NavHeaderComponent,
    Button,
    Dialog,
    FloatLabel,
    InputText,
    NgForOf,
    NgIf,
    PrimeTemplate,
    ReactiveFormsModule,
    TableModule,
    RouterLink,
    FormsModule,
    MultiSelect
  ]
})
export class DormitoryComponent implements OnInit {
  dormitories: DormitoryView[] = [];

  viewOpened = false;
  universityOptions: Selectable[] = [];
  editingForm: DormitoryRequest = {
    address: '',
    universityIds: []
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
    this.loadDormitories();
    const dormId = this.route.snapshot.queryParamMap.get('id');
    if (Number(dormId)) {
      this.editClick(Number(dormId));
    }
  }

  loadDormitories(): void {
    this.dormitoryRepository.getAll().subscribe({
      next: (dormitories) => {
        this.universityRepository.getAll().subscribe({
          next: (universities) => {
            this.dormitories = dormitories.map(d => ({
              ...d,
              universities: universities.filter(u => d.universityIds.includes(u.id))
            }));
            this.universityOptions = universities.map(u => ({id: u.id, name: u.name}));
          }
        });
      }
    });
  }

  editClick(id: number) {
    this.dormitoryRepository.get(id).subscribe({
      next: (dormitory) => this.editingForm = dormitory
    });

    this.editingId = id;
    this.openView();
    Utils.changeQueryParam(this.route, this.router, { id: id });
  }

  deleteClick(d: Dormitory) {
    if (d.residentNumber) {
      this.toast.warn('Delete failed', 'This dormitory has residents');
    } else {
      this.dormitoryRepository.delete(d.id).subscribe({
        next: () => this.loadDormitories()
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
      address: '',
      universityIds: []
    };
    Utils.changeQueryParam(this.route, this.router, { id: undefined });
  }

  saveView(): void {
    if (!this.editingId) {
      this.dormitoryRepository.add(this.editingForm).subscribe({
        next: () => {
          this.loadDormitories();
          this.closeView();
        }
      });
    } else {
      const srcDorm = this.dormitories.filter(d => d.id === this.editingId)[0];
      const srcDormUIds = srcDorm.universities.map(u => u.id);
      const universityWasRemoved = !srcDormUIds.every(uId => this.editingForm.universityIds.includes(uId));
      const addressWasChanged = srcDorm.address !== this.editingForm.address;

      if ((addressWasChanged || universityWasRemoved) && srcDorm.residentNumber) {
        this.toast.warn('Update failed', 'You cannot remove university or change address if dormitory still has residents');
      } else {
        this.dormitoryRepository.update(this.editingId, this.editingForm).subscribe({
          next: () => {
            this.loadDormitories();
            this.closeView();
          }
        });
      }
    }
  }
}
