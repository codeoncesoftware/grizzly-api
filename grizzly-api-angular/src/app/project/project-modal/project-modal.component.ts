import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { Project } from 'src/app/shared/models/Project';
import { ProjectService } from '../project.service';

// REDUX
import { Store } from '@ngrx/store';
import { ProjectsState } from '../../store/project/project.state';

import * as project from '../../store/project/project.actions';
import * as dbsourceActions from '../../store/dbsource/dbsource.actions';
import { DBSource } from 'src/app/shared/models/DBSource';
import { Database } from 'src/app/shared/models/Database';
import { MatStepper } from '@angular/material';
import { DbsourceModalComponent } from 'src/app/dbsource/dbsource-modal/dbsource-modal.component';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { DBSourceService } from 'src/app/dbsource/dbsource.service';

@Component({
    selector: 'app-project-modal',
    templateUrl: './project-modal.component.html',
    styleUrls: ['./project-modal.component.scss']
})
export class ProjectModalComponent implements OnInit {

    // Store old project Name to save Project Name Unicity check when name is not changed
    oldProjectName: string;
    // Variable to store our project informations
    obj = new Project();
    // Store our project informations to keep same data if update is cancelled
    projectToUpdate = new Project();

    uniqueProjectName = true;
    uniqueDbName = true;
    reloadProjects = false;

    dbsourcesList: DBSource[];
    databasesList: Database[];
    dataSourceName: string;
    databaseName: string;

    createDefaultDb = false;

    /** Stepper Component */
    @ViewChild('stepper', { static: true }) stepper: MatStepper;

    constructor(private store: Store<ProjectsState>,
                private dbstore: Store<DBSourcesState>,
                private projectService: ProjectService,
                private dbSourceService: DBSourceService,
                public dialogRef: MatDialogRef<ProjectModalComponent>,
                public dialog: MatDialog,
                @Inject(MAT_DIALOG_DATA) public data: any) {
    }

    ngOnInit() {
        // If a select database button is clicked, open the second step
        if (this.data.step) {
            this.stepper.selectedIndex = this.data.step;
        }
        // Make a Copy of the Object
        this.obj = { ...this.data.project };
        // Get Objects From State
        this.store.select('projects').subscribe(resState => {
            if (this.data.update === true) {
                // tslint:disable-next-line: no-string-literal
                this.obj = resState['active'];
            }
            this.store.select('dbsources').subscribe((res: DBSource[]) => {
                // tslint:disable-next-line: no-string-literal
                this.dbsourcesList = res['dbsources'];
                if (this.obj.dbsourceId) {
                    this.selectDataSource(this.obj.dbsourceId);
                }
            });
        });
    }

    selectDataSource(dbsourceId) {
        const index = this.dbsourcesList.findIndex((db) => db.id === dbsourceId);
        if (index >= 0) {
            this.dataSourceName = this.dbsourcesList[index].name;
            this.databasesList = this.dbsourcesList[index].databases;
        }
    }

    onInput() {
        this.uniqueProjectName = true;
    }

    // Create a New Project With the given form
    public validateProject() {

        // in case of an update, no need to check if it's the same name
        let test = false;
        if (this.obj.name !== this.data.project.name) {
            test = true;
        }
        this.projectService.checkProjectNameUnicity(this.obj.name, this.obj.id).subscribe(res => {
            if (res === true && test) {
                this.uniqueProjectName = false;
                this.stepper.selectedIndex = 0;
            } else {
                this.obj.userEmail = localStorage.getItem('userEmail');
                this.uniqueProjectName = true;
                if (this.data.action.create === true) {
                    if (this.createDefaultDb) {
                        this.createProjectWithDefaultDb();
                    } else {
                        this.store.dispatch(new project.AddProject(this.obj));
                    }
                } else {
                    this.store.dispatch(new project.UpdateProject(this.obj));
                }
                this.dialogRef.close();
            }
        }, error => {
        });
    }

    private createProjectWithDefaultDb() {
        const db = new DBSource();
        // DB name can not contain spaces
        db.name = this.obj.name.replace(new RegExp(' ', 'g'), '_');
        db.userEmail = this.obj.userEmail;
        db.database = db.name;
        this.dbSourceService.checkUnicity(db.name, null).subscribe(res => {
            if (res === true) {
                this.dbSourceService.saveDBSource(db).subscribe((dbRes) => {
                    this.dbstore.dispatch(new dbsourceActions.AddDBSourceSuccess(dbRes));
                    this.obj.dbsourceId = dbRes.id;
                    this.obj.databaseName = dbRes.name;
                    this.store.dispatch(new project.AddProject(this.obj));
                    this.dialogRef.close();
                });
            } else {
                this.uniqueDbName = false;
            }
        });

    }

    public openDataSourceModal() {
        this.dialog.open(DbsourceModalComponent,
            { // Modal configuration
                width: '70%',
                position: {
                    top: '15vh'
                },
                data: {
                    dbsource: null
                },
            });
    }

    /** MatStepper Actions Control */
    public goBack(stepper: MatStepper): void {
        stepper.previous();
    }

    public goForward(stepper: MatStepper): void {
        this.projectService.checkProjectNameUnicity(this.obj.name, this.obj.id).subscribe(res => {
            if (res === true) {
                this.uniqueProjectName = false;
            } else {
                stepper.next();
            }
        });
    }
}
