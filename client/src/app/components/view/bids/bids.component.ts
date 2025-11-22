import {Component, OnInit} from '@angular/core';
import {NavHeaderComponent} from '../../common/nav-header/nav-header.component';
import {AuthService} from '../../../services/auth.service';
import {
  Bid,
  BID_STATUS_COLOR_MAP,
  BID_TYPE_MAP,
  BID_TYPE_NON_RESIDENT_OPTIONS,
  BID_TYPE_RESIDENT_OPTIONS,
  BidStatus,
  BidType,
  DepartureBid,
  DepartureData,
  isEditableBidStatus,
  OccupationBid,
  RoomChangeBid
} from '../../../models/bid/bid.model';
import {BidRepository} from '../../../repositories/bid.repository';
import {Role} from '../../../models/auth/role.model';
import {Tab, TabList, TabPanel, TabPanels, Tabs} from 'primeng/tabs';
import {NgForOf, NgIf} from '@angular/common';
import {BidCardComponent} from './bid-card/bid-card.component';
import {ActivatedRoute, Router} from '@angular/router';
import {Utils} from '../../../services/utils';
import {Dialog} from 'primeng/dialog';
import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {Textarea} from 'primeng/textarea';
import {PrimeTemplate} from 'primeng/api';
import {University} from '../../../models/university/university.model';
import {Dormitory} from '../../../models/dormitory/dormitory.model';
import {localizeRoomType, Room, ROOM_TYPE_OPTIONS, RoomType} from '../../../models/room/room.model';
import {UniversityRepository} from '../../../repositories/university.repository';
import {DormitoryRepository} from '../../../repositories/dormitory.repository';
import {RoomRepository} from '../../../repositories/room.repository';
import {FormsModule} from '@angular/forms';
import {FileRepository} from '../../../repositories/file.repository';
import {Calendar} from 'primeng/calendar';
import {Select} from 'primeng/select';
import {InputNumber} from 'primeng/inputnumber';
import {FileUpload} from 'primeng/fileupload';
import {BidRequest, DepartureRequest, OccupationRequest, RoomChangeRequest} from '../../../models/bid/bid.request';
import {ToastService} from '../../../services/toast.service';

interface OccupationDataView {
  university: University;
  dormitory: Dormitory;
}

interface DepartureDataView extends DepartureData {}

interface RoomChangeDataView {
  roomTo: Room;
  roomPreferType?: RoomType;
}

interface RoomView {
  id: number;
  label: string;
}

@Component({
  selector: 'bids-view',
  standalone: true,
  templateUrl: './bids.component.html',
  imports: [
    NavHeaderComponent,
    Tabs,
    TabList,
    Tab,
    TabPanels,
    NgIf,
    TabPanel,
    BidCardComponent,
    NgForOf,
    Dialog,
    Button,
    InputText,
    Textarea,
    PrimeTemplate,
    FormsModule,
    Calendar,
    Select,
    InputNumber,
    FileUpload
  ]
})
export class BidsComponent implements OnInit {
  selfBids: Bid[] = [];

  openedTypes: BidType[] = [];

  currTabIndex = 0;
  inProcessBids: Bid[] = [];
  pendingBids: Bid[] = [];
  archivedBids: Bid[] = [];

  viewOpened = false;
  isNewBid = false;
  viewBid?: Bid;
  occupationData?: OccupationDataView;
  departureData?: DepartureDataView;
  roomChangeData?: RoomChangeDataView;
  universityOptions: University[] = [];
  dormitoryOptions: Dormitory[] = [];
  roomOptions: RoomView[] = [];

  comment = '';
  commentDialogOpened = false;
  isCommentForDeny = false;

  constructor(
    protected authService: AuthService,
    protected fileRepository: FileRepository,
    private bidRepository: BidRepository,
    private universityRepository: UniversityRepository,
    private dormitoryRepository: DormitoryRepository,
    private roomRepository: RoomRepository,
    private route: ActivatedRoute,
    private router: Router,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['id']) {
        this.openBid(Number(params['id']), this.authService.getRole() !== Role.MANAGER);
      }
    });
    if (this.authService.getRole() === Role.MANAGER) {
      this.loadInProcessBids();
    } else {
      this.loadSelfBids();
      this.loadOpenedTypes();
    }
  }

  loadSelfBids(): void {
    this.bidRepository.getSelf().subscribe({
      next: (res) => this.selfBids = res
    });
  }

  loadOpenedTypes(): void {
    this.bidRepository.getSelfOpenedBidTypes().subscribe({
      next: (res) => this.openedTypes = res
    });
  }

  loadInProcessBids(): void {
    this.bidRepository.getInProcess().subscribe({
      next: (res) => this.inProcessBids = res
    });
  }

  loadPendingBids(): void {
    this.bidRepository.getPending().subscribe({
      next: (res) => this.pendingBids = res
    });
  }

  loadArchivedBids(): void {
    this.bidRepository.getArchived().subscribe({
      next: (res) => this.archivedBids = res
    });
  }

  onTabChange(index: any) {
    switch (Number(index)) {
      case 0:
        this.loadInProcessBids();
        break;
      case 1:
        this.loadPendingBids();
        break;
      case 2:
        this.loadArchivedBids();
        break;
      default:
        console.error("Invalid tab event: ", index);
    }
    this.currTabIndex = Number(index);
  }

  openBid(number: any, toEdit: boolean) {
    this.bidRepository.get(Number(number)).subscribe({
      next: (bid) => {
        this.viewBid = bid;

        if (this.viewBid.type === BidType.OCCUPATION) {
          const occBid = (this.viewBid as OccupationBid);
          this.occupationData = {
            university: {} as University,
            dormitory: {} as Dormitory
          };

          this.universityRepository.get(occBid.universityId).subscribe({
            next: (university) => this.occupationData!.university = university
          });
          this.dormitoryRepository.get(occBid.dormitoryId).subscribe({
            next: (dormitory) => {
              this.occupationData!.dormitory = dormitory;
              this.dormitoryOptions = [dormitory];
            }
          });

          if (toEdit && isEditableBidStatus(this.viewBid.status)) {
            this.loadDormOptions(occBid.universityId);
          }
        } else if (this.viewBid.type === BidType.DEPARTURE) {
          const depBid = (this.viewBid as DepartureBid);
          this.departureData = {
            dayFrom: new Date(depBid.dayFrom),
            dayTo: new Date(depBid.dayTo)
          };
        } else if (this.viewBid.type === BidType.ROOM_CHANGE) {
          const rcBid = (this.viewBid as RoomChangeBid);
          this.roomChangeData = {
            roomTo: {} as Room,
            roomPreferType: rcBid.roomPreferType ? rcBid.roomPreferType : 0 as unknown as RoomType
          };

          if (rcBid.roomToId) {
            this.roomRepository.get(rcBid.roomToId).subscribe({
              next: (room) => {
                this.roomChangeData!.roomTo = room;
                this.roomOptions = [{id: room.id, label: `${room.number}: ${room.floor} этаж, ${localizeRoomType(room.type)}`}];
              }
            });
          }

          if (toEdit && isEditableBidStatus(this.viewBid.status)) {
            this.loadRoomOptions();
          }
        }

        this.viewOpened = true;
      }
    });
    this.loadUniversityOptions();
  }

  newBid() {
    this.viewOpened = true;
    this.isNewBid = true;
    this.viewBid = {
      number: 0,
      sender: undefined,
      text: '',
      type: this.getBidTypeOptions()[0].id,
      status: BidStatus.IN_PROCESS,
      attachments: []
    };
    if (this.authService.getRole() === Role.RESIDENT) {
      this.loadRoomOptions();
    }
    this.updateBidTypeForm();
    this.loadUniversityOptions();
  }

  updateBidTypeForm() {
    this.occupationData = undefined;
    this.departureData = undefined;
    this.roomChangeData = undefined;
    switch (this.viewBid?.type) {
      case BidType.OCCUPATION:
        this.occupationData = {
          university: {} as University,
          dormitory: {} as Dormitory,
        };
        break;
      case BidType.DEPARTURE:
        this.departureData = {} as DepartureDataView;
        break;
      case BidType.ROOM_CHANGE:
        this.roomChangeData = {roomTo: {id: 0} as Room, roomPreferType: 0 as unknown as RoomType};
        break;
    }
  }

  loadUniversityOptions() {
    this.universityRepository.getAll().subscribe({
      next: (res) => this.universityOptions = res
    });
  }

  loadDormOptions(universityId: number) {
    this.dormitoryRepository.getAll().subscribe({
      next: (res) => this.dormitoryOptions = res.filter(d => d.universityIds.includes(universityId))
    });
  }

  loadRoomOptions() {
     this.roomRepository.getAvailableForResident().subscribe({
      next: (res) => this.roomOptions = res
        .map(r => ({id: r.id, label: `${r.number}: ${r.floor} этаж, ${localizeRoomType(r.type)}`}))
        .concat([{id: 0, label: '-'}])
    });
  }

  onBidClick(number: any) {
    Utils.changeQueryParam(this.route, this.router, { id: number });
  }

  closeView(): void {
    this.viewOpened = false;
    this.resetForms();
    Utils.changeQueryParam(this.route, this.router, { id: null });
  }

  resetForms() {
    this.isNewBid = false;
    this.viewBid = undefined;
    this.occupationData = undefined;
    this.roomChangeData = undefined;
    this.departureData = undefined;
    this.comment = '';
    this.isCommentForDeny = false;
  }

  redirectToSender(): void {
    if (!this.viewBid || !this.viewBid.sender ||
      this.viewBid.sender.role !== Role.RESIDENT ||
      this.authService.getRole() !== Role.MANAGER
    ) {
      return;
    }
    this.router.navigate(['residents'], { queryParams: { search: this.viewBid.sender.login } })
  }

  denyBid() {
    this.commentDialogOpened = true;
    this.isCommentForDeny = true;
  }

  acceptBid() {
    this.bidRepository.accept(this.viewBid!.number).subscribe({
      next: () => {
        this.closeView();
        this.onTabChange(this.currTabIndex);
      }
    });
  }

  pendBid() {
    this.commentDialogOpened = true;
    this.isCommentForDeny = false;
  }

  cancelComment() {
    this.commentDialogOpened = false;
  }

  saveComment() {
    if (this.isCommentForDeny) {
      this.bidRepository.deny(this.viewBid!.number, this.comment).subscribe({
        next: () => {
          this.commentDialogOpened = false;
          this.closeView();
          this.onTabChange(this.currTabIndex);
        }
      });
    } else {
      this.bidRepository.pend(this.viewBid!.number, this.comment).subscribe({
        next: () => {
          this.commentDialogOpened = false;
          this.closeView();
          this.onTabChange(this.currTabIndex);
        }
      });
    }
  }

  isEditable(): boolean {
    return this.isNewBid || (!!this.viewBid && !!this.viewBid.sender &&
      this.viewBid.sender.login === this.authService.getLogin() &&
      isEditableBidStatus(this.viewBid.status));
  }

  onFileUpload(event: any): void {
    const downloadKey: string = event.originalEvent.body.data;
    const filename: string = event.files[0].name;
    this.viewBid?.attachments.push({ downloadKey: downloadKey, filename: filename });
  }

  uploadLink(): string {
    return this.fileRepository.uploadLink();
  }

  removeFile(key: string): void {
    this.viewBid!.attachments = this.viewBid?.attachments.filter(a => a.downloadKey !== key)!;
  }

  saveBid() {
    if (!this.viewBid) return;
    if (!this.isBidValid()) {
      this.toast.warn('Некорректная форма');
      return;
    }

    if (this.isNewBid) {
      this.bidRepository.createBid(this.mapViewToRequest()!, this.viewBid.type).subscribe({
        next: () => {
          this.closeView();
          this.loadSelfBids();
          this.loadOpenedTypes();
        }
      });
    } else {
      this.bidRepository.editBid(this.viewBid.number, this.mapViewToRequest()!, this.viewBid.type).subscribe({
        next: () => {
          this.closeView();
          this.loadSelfBids();
          this.loadOpenedTypes();
        }
      });
    }
  }

  getBidTypeOptions() {
    switch (this.authService.getRole()) {
      case Role.NON_RESIDENT:
        return BID_TYPE_NON_RESIDENT_OPTIONS.filter(o => !this.openedTypes.includes(o.id));
      case Role.RESIDENT:
        return BID_TYPE_RESIDENT_OPTIONS.filter(o => !this.openedTypes.includes(o.id));
      default:
        return [];
    }
  }

  private isBidValid(): boolean {
    const oneDayMillis = 24 * 60 * 60 * 1000;
    return !!this.viewBid && !!this.viewBid.text &&
      (this.viewBid.type !== BidType.DEPARTURE || (
        !!this.departureData &&
        this.departureData.dayTo.getTime() - this.departureData.dayFrom.getTime() >= oneDayMillis &&
        this.departureData.dayTo.getTime() - this.departureData.dayFrom.getTime() <= 60 * oneDayMillis
      )) &&
      (this.viewBid.type !== BidType.ROOM_CHANGE || (
        !!this.roomChangeData && (!!this.roomChangeData.roomTo.id || !!this.roomChangeData.roomPreferType) &&
        !(!!this.roomChangeData.roomTo.id && !!this.roomChangeData.roomPreferType)
      )) &&
      (this.viewBid.type !== BidType.OCCUPATION || (
        !!this.occupationData && !!this.occupationData.university.id && !!this.occupationData.dormitory.id
      ));
  }

  private mapViewToRequest(): BidRequest | OccupationRequest | DepartureRequest | RoomChangeRequest | undefined {
    if (!this.viewBid) return undefined;
    let request: BidRequest = {
      text: this.viewBid.text,
      attachmentKeys: this.viewBid.attachments.map(a => a.downloadKey)
    };
    switch (this.viewBid.type) {
      case BidType.DEPARTURE:
        return {...request, dayFrom: this.departureData?.dayFrom, dayTo: this.departureData?.dayTo};
      case BidType.ROOM_CHANGE:
        return {...request,
          roomToId: !!this.roomChangeData?.roomTo.id ? this.roomChangeData.roomTo.id : undefined,
          roomPreferType: !!this.roomChangeData?.roomPreferType ? this.roomChangeData.roomPreferType : undefined
        };
      case BidType.OCCUPATION:
        return {...request,universityId: this.occupationData?.university.id, dormitoryId: this.occupationData?.dormitory.id};
      case BidType.EVICTION:
        return request;
    }
  }

  protected readonly Role = Role;
  protected readonly BID_TYPE_MAP = BID_TYPE_MAP;
  protected readonly BID_STATUS_COLOR_MAP = BID_STATUS_COLOR_MAP;
  protected readonly Utils = Utils;
  protected readonly ROOM_TYPE_OPTIONS = ROOM_TYPE_OPTIONS;
}
