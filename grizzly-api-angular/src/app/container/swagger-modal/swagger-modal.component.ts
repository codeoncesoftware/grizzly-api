import { Component, OnInit, ElementRef } from '@angular/core';
import SwaggerUI from 'swagger-ui';
import { Store } from '@ngrx/store';
import { ContainerState } from 'src/app/store/container/container.state';
import { Container } from 'src/app/shared/models/Container';
import { environment } from 'src/environments/environment';
import { ContainerService } from '../container.service';
import * as fileSaver from 'file-saver';
import { ToastrService } from 'ngx-toastr';
import { ProjectsState } from 'src/app/store/project/project.state';
import { Project } from 'src/app/shared/models/Project';

@Component({
  selector: 'app-swagger-modal',
  templateUrl: './swagger-modal.component.html',
  styleUrls: ['./swagger-modal.component.scss']
})
export class SwaggerModalComponent implements OnInit {

  baseUrl: string = environment.baseUrl;

  activeContainer: Container;
  activeProject: Project;

  showInspector = false;
  postmanChoice: number;
  panelOpenState: boolean;
  swaggerBaseUrl: string;
  containerServerUrl: string;

  ui: any;

  constructor(
    private el: ElementRef,
    private containerService: ContainerService,
    private containerStore: Store<ContainerState>,
    private projectStore: Store<ProjectsState>,
    private toaster: ToastrService) { }

  ngOnInit() {
    this.containerStore.select('containers').subscribe(res => {
      // tslint:disable-next-line: no-string-literal
      this.activeContainer = res['active'];
      this.swaggerBaseUrl = window.location.origin + '/api/swagger/' + this.activeContainer.id + '/' + this.activeContainer.swaggerUuid;
      this.containerServerUrl = window.location.origin + this.containerService.getContainerServerUrl(this.activeContainer.id);
    });
    this.projectStore.select('projects').subscribe(res => [
      // tslint:disable-next-line: no-string-literal
      this.activeProject = res['active']
    ]);
  }

  openInspector() {
    this.showInspector = true;
    if (this.showInspector === true && !this.ui) {
      this.ui = SwaggerUI({
        url: this.swaggerBaseUrl,
        domNode: this.el.nativeElement.querySelector('.swagger-container'),
        deepLinking: true,
        presets: [
          SwaggerUI.presets.apis
        ],
      });
      // Back to Top
      window.scrollTo({ top: 0 });
    }
  }

  /** Generate and Download the corresponding Swagger.json File for the Given Container Id & Name */
  downloadGeneratedSwagger(mode) {
    this.containerService.getGeneratedSwagger(this.activeContainer.id, mode).subscribe(response => {
      this.saveFile(response.body, response.headers.get('fileName'));
    });
  }

  /** Save the Generated Json File */
  saveFile(data: any, filename?: string) {
    const blob = new Blob([data], { type: 'text/csv; charset=utf-8' });
    fileSaver.saveAs(blob, filename);
  }

  onCollapse(index: number) {
    if (index === 0) {
      this.showInspector = false;
      this.ui = null;
      this.el.nativeElement.querySelector('.swagger-container').innerHTML = ' ';
    }
  }

  /**
   * Copy the Swagger JSON content for the active version to clipboard
   */
  copySwaggerJson() {
    this.containerService.getGeneratedSwagger(this.activeContainer.id, 'prod').subscribe(response => {
      this.copy(response.body);
      this.toaster.success('Swagger Json copied to clipboard');
    });
  }

  /**
   * Copy the passed value to to clipboard'
   * @param json to copy
   */
  copy(json: string) {
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = json;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.toaster.success('Copied to clipboard');
  }

}
