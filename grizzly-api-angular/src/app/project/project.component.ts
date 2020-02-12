import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Project } from '../shared/models/Project';
import { MatDialog } from '@angular/material/dialog';
import { ProjectModalComponent } from './project-modal/project-modal.component';
import { Router } from '@angular/router';

// NGRX
import { FormGroup, Validators, FormBuilder } from '@angular/forms';

import { Store } from '@ngrx/store';
import { ProjectsState } from '../store/project/project.state';

import * as project from '../store/project/project.actions';
import { SlideInOutAnimation } from '../shared/animations';
import { DBSource } from '../shared/models/DBSource';
import { ProjectService } from './project.service';
import { ContainerService } from '../container/container.service';

import { Security } from '../shared/models/Security';
import { RoleModalComponent } from './role-modal/role-modal.component';
import { ToastrService } from 'ngx-toastr';
import { MessageService } from '../shared/message-modal/message.service';


@Component({
    selector: 'app-project',
    templateUrl: './project.component.html',
    styleUrls: ['./project.component.scss'],
    animations: [SlideInOutAnimation]
})

export class ProjectComponent implements OnInit {

    project: Project;
    projectToSave = new Project();

    security = new Security();
    id: string;
    newProjectDescription: string;
    toggleProjectDetails = false;

    dbsourcesList: DBSource[];
    dataSourceName: string;
    databaseName: string;

    role: string;
    oldRoles: string[];
    modified: boolean;
    unique = true;
    firstProjectId: string;
    containerId: string;
    baseUrl: string;

    @ViewChild('datasourceSelect', { static: false }) datasourceSelect;
    @ViewChild('databaseSelect', { static: false }) databaseSelect;

    @ViewChild('signinCurl', { static: false }) signinCurl: ElementRef;
    @ViewChild('signinAngular', { static: false }) signinAngular: ElementRef;
    @ViewChild('signupCurl', { static: false }) signupCurl: ElementRef;
    @ViewChild('signupAngular', { static: false }) signupAngular: ElementRef;
    @ViewChild('securedCurl', { static: false }) securedCurl: ElementRef;
    @ViewChild('securedAngular', { static: false }) securedAngular: ElementRef;
    @ViewChild('signinModal', { static: false }) signinModal: ElementRef;
    @ViewChild('signupModal', { static: false }) signupModal: ElementRef;


    // Boolean for Disabling Datasources Select
    changeDatasource = true;
    securityForm: FormGroup;
    firstContainerId: string;

    constructor(private router: Router,
        // tslint:disable: align
        private store: Store<ProjectsState>,
        private dialog: MatDialog,
        private messageBoxService: MessageService,
        private formBuilder: FormBuilder,
        private toaster: ToastrService,
        private projectService: ProjectService,
        private containerService: ContainerService
    ) {
    }
    // tslint:enable: align
    ngOnInit() {
        this.baseUrl = window.location.origin;
        this.store.select('projects').subscribe(resState => {
            // tslint:disable-next-line: no-string-literal
            this.project = resState['active'];

            this.projectToSave = Object.assign({}, this.project);
            this.store.select('dbsources').subscribe((res: DBSource[]) => {
                // tslint:disable-next-line: no-string-literal
                this.dbsourcesList = res['dbsources'];
                if (this.project.dbsourceId) {
                    this.selectDataSource(this.project.dbsourceId);
                }
            });

        });
        this.oldRoles = Object.assign([], this.project.roles);
        this.projectService.getAllProjects().subscribe(res => {
            this.firstProjectId = res[0].id;
            this.containerService.getAllContainers(res[0].id).subscribe(containers => {
                this.firstContainerId = containers[0].id;
            });
        });
        this.store.select('containers').subscribe(resState => {
            // tslint:disable-next-line: no-string-literal
            this.containerId = resState['active'].id;
        });
        this.securityForm = this.formBuilder.group({
            clientId: ['', Validators.required],
            clientSecret: ['', [Validators.required, Validators.minLength(64), Validators.pattern('[a-zA-Z0-9]*')]],
            tokenExpiration: ['', Validators.required]
        });

    }


    public openProjectModal(index?: number) {
        this.dialog.open(ProjectModalComponent,
            { // Modal configuration
                width: '50%',
                height: '75vh',
                position: {
                    top: '15vh'
                },
                data: {
                    project: this.project,
                    action: {
                        update: true,
                        create: false,
                        msg: 'popups.project.edit'
                    },
                    step: index
                },
            });
    }

    selectDataSource(dbsourceId) {
        const index = this.dbsourcesList.findIndex((db) => db.id === dbsourceId);
        if (this.dbsourcesList[index]) {
            this.dataSourceName = this.dbsourcesList[index].name;
        }

    }

    showProjectDetails() {
        this.toggleProjectDetails = !this.toggleProjectDetails;
    }

    // Open the modal to get confirmation for delete
    public openConfirmDeleteDialog() {
        // this.messageBoxService.openWarning('popups.project.delete.title', 'popups.project.delete.msg', { projectName: this.project.name });
        this.messageBoxService.openWarning('popups.project.delete.title', 'popups.project.delete.msg',
            {
                projectName: this.project.name,
                info: {
                    msg: 'messageBox.project.delete',
                    infos: ['messageBox.project.msgDeleteAllVersion', 'messageBox.project.msgDeleteAllFiles']
                }
            }).afterClosed().subscribe((data) => {
                if (data) {
                    this.store.dispatch(new project.DeleteProject(this.project.id));
                    if (!this.project.id) {
                        this.router.navigate(['/app/dashboard']);
                    } else {
                        this.router.navigate(['/app/project/', this.project.id]);
                    }
                }
            });

    }

    saveSecurityConfig() {
        this.store.dispatch(new project.UpdateProject(this.project));
    }

    addRoles() {
        this.dialog.open(RoleModalComponent,
            { // Modal configuration
                width: '50%',
                position: {
                    top: '15vh'
                },
                data: {
                    project: this.project,
                    action: {
                        msg: 'Add Roles'
                    },
                },
            });
    }

    public addNewRole() {
        this.unique = true;
        if (this.role) {
            this.project.roles.forEach(element => {
                if (this.role === element) {
                    this.unique = false;
                }
            });
            if (this.unique) {
                this.project.roles.push(this.role);
            }
            this.modified = true;

        }
        this.role = '';
    }

    /** DELETE Role with a click */
    public deleteRole(i: number) {
        this.modified = true;
        this.project.roles.splice(i, 1);
    }

    confirmChoice() {
        this.store.dispatch(new project.UpdateProject(this.project));
    }
    onInput() {
        this.unique = true;
    }

    copyText(element) {
        const text = element.innerHTML.trim();
        const selBox = document.createElement('textarea');
        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = text;
        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();
        document.execCommand('copy');
        document.body.removeChild(selBox);
        this.toaster.success('Code copied to clipboard');
    }

}
