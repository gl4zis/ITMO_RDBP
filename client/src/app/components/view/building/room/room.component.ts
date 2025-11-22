import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {NavHeaderComponent} from '../../../common/nav-header/nav-header.component';
import {Button} from 'primeng/button';
import {Dialog} from 'primeng/dialog';
import {FloatLabel} from 'primeng/floatlabel';
import {FormsModule} from '@angular/forms';
import {InputText} from 'primeng/inputtext';
import {NgForOf, NgIf} from '@angular/common';
import {PrimeTemplate} from 'primeng/api';
import {Table, TableModule} from 'primeng/table';
import {MultiSelect} from 'primeng/multiselect';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {ToastService} from '../../../../services/toast.service';
import {Utils} from '../../../../services/utils';
import {Room, ROOM_TYPE_OPTIONS, RoomType} from '../../../../models/room/room.model';
import {RoomRepository} from '../../../../repositories/room.repository';
import {RoomRequest} from '../../../../models/room/room.request';
import {DormitoryRepository} from '../../../../repositories/dormitory.repository';
import {Dormitory} from '../../../../models/dormitory/dormitory.model';
import {IconField} from 'primeng/iconfield';
import {InputIcon} from 'primeng/inputicon';
import {Select} from 'primeng/select';
import {InputNumber} from 'primeng/inputnumber';
import {StyleClass} from 'primeng/styleclass';

interface RoomView extends Room {
  dormitory: Dormitory;
}

@Component({
  selector: 'room-view',
  standalone: true,
  templateUrl: './room.component.html',
  imports: [
    NavHeaderComponent,
    Button,
    Dialog,
    FloatLabel,
    FormsModule,
    InputText,
    NgForOf,
    NgIf,
    PrimeTemplate,
    TableModule,
    MultiSelect,
    RouterLink,
    IconField,
    InputIcon,
    Select,
    InputNumber,
    StyleClass
  ]
})
export class RoomComponent implements OnInit, AfterViewInit {
  @ViewChild('table') table!: Table;

  rooms: RoomView[] = [];

  searchValue = '';

  viewOpened = false;
  dormitoryOptions: Dormitory[] = [];
  capacityOptions = [2, 3];
  editingForm: RoomRequest = {
    dormitoryId: 1,
    number: 1,
    type: RoomType.BLOCK,
    capacity: 2,
    floor: 1,
    cost: 1000
  };

  constructor(
    private roomRepository: RoomRepository,
    private dormitoryRepository: DormitoryRepository,
    private toast: ToastService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadRooms();
  }

  ngAfterViewInit() {
    const search = this.route.snapshot.queryParamMap.get('search');
    if (search) {
      this.searchValue = search;
      this.filter();
    }
  }

  loadRooms(): void {
    this.roomRepository.getAll().subscribe({
      next: (rooms) => this.dormitoryRepository.getAll().subscribe({
        next: (dormitories) => {
          this.rooms = rooms.map(r => ({
            ...r,
            dormitory: dormitories.find(d => d.id === r.dormitoryId)!,
          }));
          this.dormitoryOptions = dormitories;
        }
      })
    });
  }

  filter() {
    this.table.filterGlobal(this.searchValue, 'contains');
    Utils.changeQueryParam(this.route, this.router, { search: this.searchValue });
  }

  deleteClick(r: Room) {
    if (r.residents.length) {
      this.toast.warn('Delete failed', 'This room has residents');
    } else {
      this.roomRepository.delete(r.id).subscribe({
        next: () => this.loadRooms()
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
    this.editingForm = {
      dormitoryId: 1,
      number: 1,
      type: RoomType.BLOCK,
      capacity: 2,
      floor: 1,
      cost: 1000
    };
    Utils.changeQueryParam(this.route, this.router, { id: undefined });
  }

  saveView(): void {
    const hasNumberDuplicate = this.rooms
      .filter(r => r.dormitoryId === this.editingForm.dormitoryId)
      .map(r => r.number)
      .includes(this.editingForm.number);

    if (hasNumberDuplicate) {
      this.toast.warn('Adding room failed', 'Duplicate of room number in the same dormitory');
    } else {
      this.roomRepository.add(this.editingForm).subscribe({
        next: () => {
          this.loadRooms();
          this.closeView();
        }
      });
    }
  }

  protected readonly ROOM_TYPE_OPTIONS = ROOM_TYPE_OPTIONS;
}
