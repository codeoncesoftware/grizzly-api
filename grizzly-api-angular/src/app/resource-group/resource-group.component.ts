import { Component, OnInit, ViewChildren } from '@angular/core';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { ResourceGroup } from '../shared/models/ResourceGroup';
import { ResourceGroupModalComponent } from './resource-group-modal/resource-group-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmModalComponent } from '../shared/confirm-modal/confirm-modal.component';
import * as containerActions from '../store/container/container.actions';
import * as layoutActions from '../store/layout/layout.actions';

import { ContainerState } from '../store/container/container.state';
import { Store } from '@ngrx/store';
import { Container } from '../shared/models/Container';
import { Resource } from '../shared/models/Resource';
import { MatButton } from '@angular/material/button';
import { ConfirmModalService } from '../shared/confirm-modal/confirm-modal.service';
import * as _ from 'lodash';
import { MatExpansionPanel } from '@angular/material';
import { LayoutState } from '../store/layout/layout.state';
import { ResourceGroupService } from './resource-group.service';
import { SecurityModalComponent } from './security-modal/security-modal.component';
import { ResourceModalComponent } from '../resource/resource-modal/resource-modal.component';

/**
 * @title Tree with checkboxes
 */
@Component({
  selector: 'app-resource-group',
  templateUrl: 'resource-group.component.html',
  styleUrls: ['resource-group.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0', visibility: 'hidden' })),
      state('expanded', style({ height: '*', visibility: 'visible' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})

export class ResourceGroupComponent implements OnInit {

  container: Container;
  containerToSave: Container = new Container();
  displayDetailsBooolean = true;
  group = new ResourceGroup();
  groupToDeleteFrom = new ResourceGroup();
  groupToSecure = new ResourceGroup();
  displayedColumns = ['name'];
  resourcesToDelete = [];
  showDeleteResources = true;
  expandedElement: any;
  groupsNumber = 0;

  expanded: number[] = [0];

  @ViewChildren('deleteBtn') deleteBtn: MatButton[];
  @ViewChildren('rgPanel') resourceGroupsPanels: MatExpansionPanel[];

  isExpansionDetailRow = (i: number, row: object) => row.hasOwnProperty('detailRow');


  constructor(private layoutStore: Store<LayoutState>, private store: Store<ContainerState>, private dialog: MatDialog, private resourceGroupService: ResourceGroupService,
    // tslint:disable-next-line: align
    private confirmModalService: ConfirmModalService) {

  }

  ngOnInit() {
    this.store.select('containers').subscribe(resState => {
      // tslint:disable-next-line: no-string-literal
      this.container = resState['active'];
      this.groupsNumber = this.container.resourceGroups.length;
    });
    // Open First Resource Group

    this.layoutStore.select('layout').subscribe(res => {
      if (res) {
        // tslint:disable-next-line: no-string-literal
        res['containerGroups'].forEach(contRg => {
          if (contRg.containerId === this.container.id) {
            this.expanded = contRg.openGroupsIndexs;
          }

        });
      }
    });
  }

  openRg(containerId, index) {
    this.layoutStore.dispatch(new layoutActions.ToggleRg(containerId, index));
  }

  displayDetails() {
    this.displayDetailsBooolean = !this.displayDetailsBooolean;
  }

  updateResourcesToDelete(arrayOfResources: Resource[]) {
    this.resourcesToDelete = arrayOfResources;
    this.container.resourceGroups.forEach(group => {
      let emptyResourcesToDelete = true;


      group.resources.forEach(res => {
        this.resourcesToDelete.forEach(resource => {
          if (res.path === resource.resources.path && res.httpMethod === resource.resources.httpMethod && group.name === resource.group.name) {
            emptyResourcesToDelete = false;
            this.deleteBtn.forEach(btn => {
              if (btn._elementRef.nativeElement.id === group.name) {
                // tslint:disable-next-line: no-string-literal
                btn['_disabled'] = false;
              }
            });
          }

        });
      });
      this.deleteBtn.forEach(btn => {
        if (btn._elementRef.nativeElement.id === group.name && emptyResourcesToDelete) {
          // tslint:disable-next-line: no-string-literal
          btn['_disabled'] = true;
        }
      });

    });
    // restore the new format of the array
    const newResources = [];
    this.resourcesToDelete.forEach(x => newResources.push(x.resources));
    this.resourcesToDelete = newResources;

  }

  public openGroupModal() {
    this.dialog.open(ResourceGroupModalComponent,
      { // Modal configuration
        width: '50%',
        position: {
          top: '15vh'
        },
        data: {
          resourceGroup: this.group,
          container: this.container,
          action: {
            update: false,
            create: true,
            msg: 'popups.group.add'
          }
        },
      }).afterClosed().subscribe((res) => {
        if (res) {
          window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
        }
      });
  }

  public openEditGroupModal(group: ResourceGroup) {
    this.dialog.open(ResourceGroupModalComponent,
      { // Modal configuration
        width: '50%',
        position: {
          top: '15vh'
        },
        data: {
          resourceGroup: group,
          container: this.container,
          action: {
            update: true,
            create: false,
            msg: 'popups.group.edit'
          }
        },
      });

  }


  public openConfirmDeleteDialog() {


    const dialogRef = this.dialog.open(ConfirmModalComponent,
      {
        width: '40%',
        position: {
          top: '15vh'
        },
        data: {
          action: {
            msg: 'popups.group.deleteAll'
          }
        }
      });
    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.containerToSave = JSON.parse(JSON.stringify(this.container));
        this.containerToSave.resourceGroups = [];
        const msg = 'All Groups are Deleted';

        this.store.dispatch(new containerActions.UpdateContainer(this.containerToSave, msg));
      }
    });
  }

  public openConfirmDeleteOneDialog(group: ResourceGroup) {

    this.confirmModalService.openConfirm('popups.group.delete.title', 'popups.group.delete.msg', { name: group.name })
      .afterClosed().subscribe(res => {
        if (res) {
          this.containerToSave = JSON.parse(JSON.stringify(this.container));
          const msg = 'Group Deleted';
          const groupIndex = this.getGroupIndex(group);
          this.containerToSave.resourceGroups.splice(groupIndex, 1);
          this.store.dispatch(new containerActions.UpdateContainer(this.containerToSave, msg));
        }
      });
  }

  public openConfirmDeleteAllDialog(group: ResourceGroup) {
    this.confirmModalService.openConfirm('popups.resource.delete.title', 'popups.resource.delete.msg', { name: group.name })
      .afterClosed().subscribe(res => {
        if (res) {
          this.containerToSave = JSON.parse(JSON.stringify(this.container));
          this.groupToDeleteFrom = JSON.parse(JSON.stringify(group));
          const groupIndex = this.getGroupIndex(this.groupToDeleteFrom);
          this.resourcesToDelete.forEach((x: Resource) => {
            // DELETE RESOURCE FILES HERE
            // this.resourceService.deleteFile(x.fileId).subscribe();
            this.groupToDeleteFrom.resources = this.groupToDeleteFrom.resources.filter((ress: Resource) => (ress.httpMethod + ress.path) !== (x.httpMethod + x.path));
          });
          this.containerToSave.resourceGroups[groupIndex] = this.groupToDeleteFrom;
          const msg = 'all selected APIs are Secured';
          this.store.dispatch(new containerActions.UpdateContainer(this.containerToSave, msg));
        }
      });
  }

  getResourceIndex(resourceSearchIndex: Resource, groupIndex) {
    return this.container.resourceGroups[groupIndex].resources.findIndex((x: Resource) => x.name.toUpperCase() === resourceSearchIndex.name.toUpperCase());
  }

  getGroupIndex(group: ResourceGroup) {
    return this.container.resourceGroups.findIndex((x: ResourceGroup) => x.name.toUpperCase() === group.name.toUpperCase());
  }

  preventExpansion(event) {
    event.preventDefault();
  }

  showDeleteButton(group) {
    if (group.name === 'az') {
      return true;
    }
    return false;
  }

  /** Open a Modal To Add a Resource to a Specific ResourceGroup */
  public openAddApiModal(group: ResourceGroup): void {
    this.dialog.open(ResourceModalComponent,
      {
        position: {
          top: '10px',
          right: '10px'
        },
        height: '98%',
        width: '100vw',
        panelClass: 'full-screen-modal',

        data: {
          resource: new Resource(),
          Rgroup: group,
          container: this.container,
          editMode: false,
          action: {
            msg: 'popups.resource.add'
          }
        }
      });
  }


  public openSecureModal(group: ResourceGroup, securityLevel: boolean) {

    this.containerToSave = JSON.parse(JSON.stringify(this.container));
    this.groupToSecure = JSON.parse(JSON.stringify(group));
    const groupIndex = this.getGroupIndex(this.groupToSecure);

    this.dialog.open(SecurityModalComponent,
      {
        width: '50%',
        position: {
          top: '200px',
        },
        data: {
          title: 'JWT API',
          message: securityLevel === true ? 'popups.resource.jwt.enable' : 'popups.resource.jwt.disable',
          params: {
            securityLevel,
            containerToSave: this.containerToSave,
            groupToSecure: this.groupToSecure,
            groupIndex,
            resourcesToSecure: this.resourcesToDelete

          }
        }
      });
  }

  isSecuredGroup(group) {
    if (!_.isEmpty(group.resources)) {
      return group.resources.every(r => r.secured);
    }
    return false;
  }


}
