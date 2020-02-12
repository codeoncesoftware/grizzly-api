import { Component, OnInit, Input } from '@angular/core';
import { DBSource } from 'src/app/shared/models/DBSource';
import { DBSourceService } from '../dbsource.service';
import { MatDialog } from '@angular/material/dialog';
import { DbsourceModalComponent } from '../dbsource-modal/dbsource-modal.component';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { Store } from '@ngrx/store';
import * as dbsourceActions from '../../store/dbsource/dbsource.actions';
import { SlideInOutAnimation } from 'src/app/shared/animations';
import { ConfirmModalComponent } from 'src/app/shared/confirm-modal/confirm-modal.component';
import { Router } from '@angular/router';
import { ConfirmModalService } from 'src/app/shared/confirm-modal/confirm-modal.service';
import { element } from 'protractor';
import { ProjectsState } from 'src/app/store/project/project.state';
import { Project } from 'src/app/shared/models/Project';
import { MessageService } from 'src/app/shared/message-modal/message.service';
import { DbSourceModalService } from '../dbsource-modal/dbsource-modal.service';

@Component({
  selector: 'app-dbsource-details',
  templateUrl: './dbsource-details.component.html',
  styleUrls: ['./dbsource-details.component.scss'],
  animations: [SlideInOutAnimation]
})
export class DbsourceDetailsComponent implements OnInit {
  firstDbsourceId: string;
  @Input() dbsource: DBSource;
  ourCollections: string[] = ['authentication_user', 'fs.files', 'fs.chunks'];
  query: string;
  databaseName: string;
  collectionName: string;
  collectionsList: string[];
  mongoQuery: string;
  mongoAction: string;
  mongoActions: string[] = [
    'find', 'findOne',
    'findOneAndUpdate',
    'findOneAndReplace',
    'findOneAndDelete',
    'count',
    'insertOne',
    'insertMany',
    'updateOne',
    'updateMany',
    'deleteOne',
    'deleteMany',
    'aggregate'
  ];
  toggleDbsourceDetails = false;

  collectionDetails: any;

  panelOpenState = false;

  projectList: Project[];

  constructor(private router: Router,
              private store: Store<DBSourcesState>,
              private projectStore: Store<ProjectsState>,
              private dbSourceService: DBSourceService,
              private confirmModalService: ConfirmModalService,
              private messageBoxService: MessageService,
              public dialog: MatDialog,
              private dbsourceModalService: DbSourceModalService) { }

  ngOnInit() {
    this.store.select('dbsources').subscribe(resState => {
      // tslint:disable-next-line: no-string-literal
      this.dbsource = resState['active'];
      if (this.dbsource && this.dbsource.databases) {
        const index = this.dbsource.databases.findIndex(db => db.name === this.dbsource.database);
        if (this.dbsource.databases[index]) {
          this.collectionsList = this.dbsource.databases[index].collections;
        }
      }
      if (this.dbsource && !this.dbsource.active) {
        this.collectionsList = [];
      }
    });
    this.dbSourceService.getAll().subscribe(res => {
      this.firstDbsourceId = res[0].id;
    });
    // tslint:disable-next-line: no-string-literal
    this.projectStore.select('projects').subscribe(list => this.projectList = list['projects']);
  }

  checkConnection() {
    this.dbSourceService.checkConnection(this.dbsource).subscribe(res => {
    }, err => {
      if (err.status === 406) {
        if (err.error === 4061) {
          console.log('IllegalArgumentException');
        }
      }
    });
  }


  public openConfirmDeleteDialog() {
    if (this.getLinkedToProject() !== null) {
      this.messageBoxService.openError('popups.dbsource.delete.title', 'popups.dbsource.noDelete',
        {
          projectName: this.projectList.find(proj => proj.dbsourceId === this.dbsource.id).name,
          datasourceName: this.dbsource.name
        });
    } else {
      this.messageBoxService.openWarning('popups.dbsource.delete.title', 'popups.dbsource.delete.msg',
        {
          datasourceName: this.dbsource.name,
          info: {
            msg: 'messageBox.dbsource.delete',
            infos: ['messageBox.dbsource.msgDeleteAllcontent', 'messageBox.dbsource.msgNoBackup']
          }
        })
        .afterClosed().subscribe((data) => {
          if (data) {
            this.store.dispatch(new dbsourceActions.DeleteDBSource(this.dbsource.id));
            this.router.navigate(['/app/dashboard']);
          }
        });
    }
  }

  openEditDBSourceModal() {
    this.dbsourceModalService.openupdate(this.dbsource);
  }

  showDbsourceDetails() {
    this.toggleDbsourceDetails = !this.toggleDbsourceDetails;
  }

  getCollectionStats(databaseName: string, collectionName: string) {
    this.dbSourceService.getCollectionStats(this.dbsource.id, databaseName, collectionName).subscribe(res => {
      // console.log(res);
      // NOT COMPLETED
    });
  }

  confirmDropCollection(dbName: string, collName: string) {
    this.confirmModalService.openConfirm('popups.dbsource.collection.drop.title', 'popups.dbsource.collection.drop.msg', { collectionName: collName })
      .afterClosed().subscribe((data) => {
        if (data) {
          this.store.dispatch(new dbsourceActions.DropCollection({ dbsourceId: this.dbsource.id, databaseName: dbName, containerId: null, collectionName: collName }));
        }
      });
  }

  showDelete(collection: string) {
    let test = true;
    this.ourCollections.forEach(collectionName => {
      if (collectionName === collection) {
        test = false;
      }
    });
    return test;
  }

  getLinkedToProject(): string {
    const index = this.projectList.findIndex(proj => proj.dbsourceId === this.dbsource.id);
    if (index > -1) {
      return this.projectList[index].name;
    }
    return null;
  }

}
