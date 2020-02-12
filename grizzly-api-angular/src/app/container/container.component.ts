import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { Container } from '../shared/models/Container';
import { MatDialog } from '@angular/material/dialog';
import * as fileSaver from 'file-saver';

// NGRX
import { Store } from '@ngrx/store';
import * as containerActions from '../store/container/container.actions';
import { ContainerState } from '../store/container/container.state';
import { ResourceGroupComponent } from '../resource-group/resource-group.component';
import { SlideInOutAnimation } from '../shared/animations';
import { ContainerService } from './container.service';
import { environment } from 'src/environments/environment';
import { FileUploader } from 'ng2-file-upload';
import { ToastrService } from 'ngx-toastr';
import { SwaggerUrlModalComponent } from './swagger-url-modal/swagger-url-modal.component';
import { ConfirmModalService } from '../shared/confirm-modal/confirm-modal.service';
import { ImportModalService } from './import-modal/impor-modal.service';
import { ContainerModalService } from './container-modal/container-modal.service';
import { LayoutState } from '../store/layout/layout.state';
import { Location } from '@angular/common';

import * as layoutActions from '../store/layout/layout.actions';
import { ResourceModalComponent } from '../resource/resource-modal/resource-modal.component';
import { Resource } from '../shared/models/Resource';
import { SwaggerModalService } from './swagger-modal/swagger-modal.service';

@Component({
  selector: 'app-container',
  templateUrl: './container.component.html',
  styleUrls: ['./container.component.scss'],
  animations: [SlideInOutAnimation]
})
export class ContainerComponent implements OnInit {

  showHierarchy = false; // Show Hierarchy

  @Input() projectId: string;
  noFilesUploaded: boolean;
  containers: Container[] = [];
  container = new Container();
  selectedIndex = 0;
  toggleContainerDetails = true;
  lock = 0;
  baseUrl: string = environment.baseUrl;
  defaultDescription = 'this is a default description';
  firstContainerId: string;

  firstRGExpanded = false;
  public uploader: FileUploader = new FileUploader({});

  @ViewChild(ResourceGroupComponent, { static: false }) resourceGroupComponent: ResourceGroupComponent; // Recource Group Component

  constructor(private toaster: ToastrService,
    private containerService: ContainerService,
    private store: Store<ContainerState>,
    private layoutStore: Store<LayoutState>,
    private dialog: MatDialog,
    private swaggerModalService: SwaggerModalService,
    private location: Location,
    private confirmModalService: ConfirmModalService,
    private importModalService: ImportModalService,
    private containerModalService: ContainerModalService) { }

  ngOnInit() {
    this.store.select('containers').subscribe(resState => {
      // tslint:disable-next-line: no-string-literal
      this.containers = resState['containers'];
      if (this.containers.length > 0 && !this.firstRGExpanded) {
        this.firstContainerId = this.containers[0].id;
        this.containers.forEach(cont => this.layoutStore.dispatch(new layoutActions.ToggleRg(cont.id, 0)));
        this.firstRGExpanded = true;
      }
      // Load Active Container To State One Time Only
      if ((this.lock === 0) && (this.containers.length > 0)) {
        this.store.dispatch(new containerActions.LoadActiveContainer(this.containers[0]));
        this.lock++;
      }
      // tslint:disable-next-line: no-string-literal
      this.container = resState['active'];
      if (this.container.hierarchy && this.container.hierarchy !== 'none') {
        this.noFilesUploaded = false;
      } else {
        this.noFilesUploaded = true;
      }
      this.selectedIndex = this.containers.findIndex(cont => cont.id === this.container.id);
    });
  }

  /** Change Active Container On Mat Tab Change */
  updateActiveState(event) {
    // Dispatch action to change the Active Container in the State
    this.store.dispatch(new containerActions.LoadActiveContainerById(this.containers[event.index].id));
    // Change URL Params so on refresh the Active Container will be selected (by Resolver)
    this.location.go('/app/project/' + this.containers[event.index].projectId + '/' + this.containers[event.index].id);
  }

  /** Toggle A Container's Hierarchy */
  showProjectHierarchy() {
    this.showHierarchy = !this.showHierarchy;
  }

  /** Show Project Hierarchy */
  public openHierarchyModal() {
    this.importModalService.openImport({
      showLoadedHierarchy: true,
      showTree: true
    });
  }

  /** Toggle A Container's Details */
  showContainerDetails() {
    this.toggleContainerDetails = !this.toggleContainerDetails;
  }

  /** Open Dialog for Container Import */
  openImportContainerModal() {
    this.containerModalService.openContainerModal({
      projectId: this.projectId,
      showImportSection: true,
      swagger: false,
      import: 'Import Container with Files',
      action: {
        msg: 'Import Container',
      }
    });
  }

  /** Open Dialog for Container Creation */
  public openAddContainerModal() {
    this.containerModalService.openContainerModal({
      projectId: this.projectId,
      showImportSection: true,
      swagger: true,
      edit: false,
      import: 'Import Swagger File',
      action: {
        msg: 'popups.container.add'
      }
    });
  }

  // Open dialog for cloning
  public openCloneContainerModal(cont: Container) {
    this.containerModalService.openContainerModal({
      projectId: this.projectId,
      container: cont,
      showImportSection: false,
      action: {
        edit: false,
        clone: true,
        msg: 'popups.container.clone'
      }
    });
  }

  /** Open Dialog To Get Confirmation To Delete All a Project's Containers */
  openConfirmDeleteAllDialog() {
    this.confirmModalService.openConfirm('popups.container.deleteAll', '', {})
      .afterClosed().subscribe((data) => {
        if (data) {
          this.deleteContainers(this.projectId);
        }
      });

  }

  /** Delete All Containers For A Single Project Based Onthe Given ProjectId */
  deleteContainers(projectId: string) {
    this.store.dispatch(new containerActions.DeleteAllContainers(projectId));
  }

  /** Open dialog for delete */
  public openEditContainerModal(cont: Container) {
    this.containerModalService.openContainerModal({
      projectId: this.projectId,
      container: cont,
      showImportSection: false,
      action: {
        edit: true,
        clone: false,
        msg: 'popups.container.edit'
      }
    });

  }

  /** Open Dialog To Confirm Container Delete Action */
  public openConfirmDeleteDialog(id: string) {
    this.confirmModalService.openConfirm('popups.container.delete.title', 'popups.container.delete.msg', { version: this.container.name })
      .afterClosed().subscribe((data) => {
        if (data) {
          this.deleteContainerById(id);
        }
      });
  }

  /** Open Dialog To Import Files for A Single Project */
  public openImportProjectModal(id: string) {
    this.importModalService.openImport({
      containerID: id,
      action: {
        confirm: false,
        msg: 'Import Project'
      },
      showTree: false
    });

  }

  /** Delete container from array after delete from DB */
  public deleteContainerById(id: string) {
    this.store.dispatch(new containerActions.DeleteContainer(id));
  }

  /** Show the "No Containers" Section When the Project has No Containers To Display. In Practice, A Project Will Always Has One Container At Least. */
  hideIfEmpty() {
    if (this.containers.length !== 0) {
      return true;
    }
    return false;
  }

  /** RecourceGroupComponent methods calls */
  RGopenGroupModal() {
    this.resourceGroupComponent.openGroupModal();
  }

  /** Get Confirmation To Delete A Specific ResourceGroup */
  RGopenConfirmDeleteDialog() {
    this.resourceGroupComponent.openConfirmDeleteDialog();
  }

  /** Enable Or Disable A Single Container Based On It's ID, Diable All the Other Containers */
  enableDisableContainer(containerId) {
    this.store.dispatch(new containerActions.EnableDisableContainer(containerId));
  }

  /** export container */
  exportContainer(containerId) {
    if (!this.noFilesUploaded) {
      this.containerService.export(containerId).subscribe(res => {
        const blob = new Blob([res], {
          type: 'application/zip'
        });
        fileSaver.saveAs(blob, containerId + '.zip');
      });
    } else {
      this.downloadGeneratedSwagger(containerId);
    }

  }

  /** Generate and Download the corresponding Swagger.json File for the Given Container Id & Name */
  downloadGeneratedSwagger(containerId) {
    this.containerService.getGeneratedSwagger(containerId, 'dev').subscribe(response => {
      this.saveFile(response.body, response.headers.get('fileName'));
    });
  }

  /** Save the Generated Json File */
  saveFile(data: any, filename?: string) {
    const blob = new Blob([data], { type: 'text/csv; charset=utf-8' });
    fileSaver.saveAs(blob, filename);
  }

  importSwaggerOnExistingContainer() {
    this.containerService.importSwaggerOnExistingContainer(this.uploader.queue[0]._file, this.container.id)
      .subscribe(res => { this.dispatchActionSuccess(res); }, err => this.toaster.error(err.error.substr(err.error.indexOf(':') + 1)));
  }
  dispatchActionSuccess(res) {
    // this.container.hierarchy = JSON.stringify(res);
    this.store.dispatch(new containerActions.UpdateContainer(res, 'Swagger Imported'));
  }

  openSwaggerUrlModal() {
    this.dialog.open(SwaggerUrlModalComponent,
      {
        width: '40%',
        position: {
          top: '15vh'
        },
        data: {
          uploader: this.uploader,
          containerId: this.container.id
        }
      });
  }

  openSwaggerModal() {
    this.swaggerModalService.openModal();
  }

  /** Open a Modal To Add a Resource to a Specific ResourceGroup */
  public openAddResourceDialog(): void {
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
          // Rgroup: group,
          container: this.container,
          editMode: false,
          action: {
            msg: 'popups.resource.add'
          }
        }
      });
  }

}
