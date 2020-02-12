import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewChildren } from '@angular/core';
import { ResourceService } from '../resource.service';
import { Resource } from 'src/app/shared/models/Resource';
import { ChangeDetectorRef } from '@angular/core';

import { ResourceGroup } from 'src/app/shared/models/ResourceGroup';
import { ResourceDetailsModalComponent } from '../resource-details-modal/resource-details-modal.component';
import { ResourceModalComponent } from '../resource-modal/resource-modal.component';

import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatDialog } from '@angular/material/dialog';
import { Container } from 'src/app/shared/models/Container';

import { Store } from '@ngrx/store';
import { ContainerState } from 'src/app/store/container/container.state';
import * as containerActions from '../../store/container/container.actions';
import { Observable } from 'rxjs';
import { FileElement } from 'src/app/shared/models/FileElement';
import { FileUploader } from 'ng2-file-upload';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatTable } from '@angular/material/table';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { DBSource } from 'src/app/shared/models/DBSource';
import { ToastrService } from 'ngx-toastr';
import { ExecuteModalService } from '../execute-modal/execute-modal.service';

@Component({
  selector: 'app-resource-list',
  templateUrl: './resource-list.component.html',
  styleUrls: ['./resource-list.component.scss']
})

export class ResourceListComponent implements OnInit {

  @ViewChild('table', { static: false }) table: MatTable<Resource>;
  /** FILE EXPLORER VARIABLEs */
  public fileElements: Observable<FileElement[]>;
  currentRoot: FileElement;
  executionErrorMessage: number;
  @ViewChildren('myCheckBoxs') checkBoxs: MatCheckbox[];
  /** NG2 UPLOADER */
  public uploader: FileUploader = new FileUploader({});
  public hasBaseDropZoneOver = false;
  /** Input Params from the Parent Component */
  @Input() group: ResourceGroup;
  @Input() container: Container;
  /** Resources List to display */
  resources: Resource[] = [];
  /** MatTable Params */
  displayedColumns = ['Check', 'Method', 'File Uri', 'Type', 'Actions'];

  resourcesToDelete = [];
  secondaryFilePaths: any[] = [];

  // DBSources List
  dbSources: DBSource[] = [];
  /** Emitter to inform the Parent component about the slected Resource */
  @Output() resourcesSelected: EventEmitter<any> = new EventEmitter<any>();
  constructor(private containersStore: Store<ContainerState>,
    // tslint:disable: align
    private dbSourcesStore: Store<DBSourcesState>,
    private toaster: ToastrService,
    private dialog: MatDialog,
    private resourceService: ResourceService,
    private store: Store<ContainerState>,
    public executeModalService: ExecuteModalService) { }
  // tslint:enable: align
  ngOnInit() {
    this.store.select('containers').subscribe(resState => {
      // tslint:disable-next-line: no-string-literal
      this.container = resState['active'];
    });
    this.dbSourcesStore.select('dbsources').subscribe(res => {
      // tslint:disable-next-line: no-string-literal
      this.dbSources = res['dbsources'];
    });
  }


  /** Check if the selected resource already checked
   * Add it to resource array to delete if not found, else remove it from the array
   */
  public checkResource(resourceCheck: Resource, group: ResourceGroup): void {

    const index = this.resourcesToDelete.findIndex((x) => (x.resources.httpMethod + x.resources.path) === (resourceCheck.httpMethod + resourceCheck.path));
    const id = group.name + '#' + resourceCheck.path + '#' + resourceCheck.httpMethod;
    this.checkBoxs.forEach(x => {
      if (x.id === id) {
        if (index >= 0 && x.checked) {
          this.resourcesToDelete.splice(index, 1);
        } else {
          this.resourcesToDelete.push({ group, resources: resourceCheck });
        }
        this.resourcesSelected.emit(this.resourcesToDelete);
      }
    });

  }

  /** Open Edit Resource Modal for a given Resource Object */
  public openResourceEditModal(resourceEdit: Resource): void {
    const dialogRef = this.dialog.open(ResourceModalComponent,
      {
        position: {
          top: '10px',
          right: '10px'
        },
        height: '98%',
        width: '100vw',
        panelClass: 'full-screen-modal',
        data: {
          resource: resourceEdit,
          ressourceIndex: this.group.resources.map(x => x.name + x.path).indexOf(resourceEdit.name + resourceEdit.path),
          Rgroup: this.group,
          container: this.container,
          editMode: true,
          action: {
            msg: 'popups.resource.edit'
          }
        }
      });
    dialogRef.afterClosed().subscribe(res => {
      // FIX ME
      if (res) {
        const groupIndex = this.getGroupIndex(this.group);
        const resourceIndex = this.getResourceIndex(resourceEdit, groupIndex);
        this.container.resourceGroups[groupIndex].resources[resourceIndex] = res;
        const msg = 'Resource Updated';
        this.containersStore.dispatch(new containerActions.UpdateContainer(this.container, msg));
      }
    });

  }

  public getGroupIndex(group: ResourceGroup): number {
    return this.container.resourceGroups.findIndex((x: ResourceGroup) => x.name.toUpperCase() === group.name.toUpperCase());
  }

  public getResourceIndex(resourceSearchIndex: Resource, groupIndex): number {
    return this.container.resourceGroups[groupIndex].resources.findIndex((x: Resource) => x.name.toUpperCase() === resourceSearchIndex.name.toUpperCase());
  }

  /** Open a Modal to Show Resource Details */
  public infoApi(api: Resource): void {
    this.dialog.open(ResourceDetailsModalComponent,
      {
        width: '40%',
        position: {
          top: '15vh'
        },
        data: {
          api,
          action: {
            msg: api.path
          }
        }
      });
  }

  /** NG UPLOADER METHOD (Not Used For Now, ABANDONED) */
  public fileOverBase(ev: boolean): void {
    this.hasBaseDropZoneOver = ev;
  }

  public execute(resource: Resource): void {
    this.resourceService.execute(this.container.id, resource.path, resource.executionType.toLowerCase()).subscribe(res => {
      this.executeModalService.openExecuteModal(res);
      // const newWindow = window.open();
      // newWindow.document.write(res);
    });
  }

  public dropTable(event: CdkDragDrop<Resource[]>) {
    const prevIndex = this.group.resources.findIndex((d) => d === event.item.data);
    moveItemInArray(this.group.resources, prevIndex, event.currentIndex);
    this.table.renderRows();
    this.containersStore.dispatch(new containerActions.UpdateContainer(this.container, 'Resource Group Updated'));
  }

  public copyAPIURL(resource: Resource) {
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    const url = location.origin + '/runtime/' + this.container.id + resource.path.toLocaleLowerCase();
    selBox.value = url;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.toaster.success('Url copied to clipboard');
  }

  checkResourceState(api) {
    if (!api.executionType) {
      return true;
    }
    if (api.executionType !== 'Query') {
      let emptyBody = true;
      api.parameters.forEach(element => {
        if (element.name === 'body') {
          emptyBody = false;
        }
      });
      if (emptyBody || !api.resourceFile) {
        return true;
      }
      return false;
    }
    return false;
  }
  executeMessage(api) {
    if (this.checkResourceState(api)) {
      return 'no main file was linked OR no body is present';
    } else {
      return 'Execute';
    }
  }
}
