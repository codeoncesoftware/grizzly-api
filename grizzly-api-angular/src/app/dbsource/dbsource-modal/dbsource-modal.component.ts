import { Component, OnInit, Inject, Optional, Output, ViewChild } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatStepper } from '@angular/material';
import { DBSource } from 'src/app/shared/models/DBSource';
import { DBSourceService } from '../dbsource.service';
import { Store } from '@ngrx/store';
import * as dbsourceActions from '../../store/dbsource/dbsource.actions';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { EventEmitter } from 'events';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';

@Component({
  selector: 'app-dbsource-modal',
  templateUrl: './dbsource-modal.component.html',
  styleUrls: ['./dbsource-modal.component.scss']
})
export class DbsourceModalComponent implements OnInit {
  isFr: boolean;
  selectedLanguage: string;
  loading = false;
  connectionSucceeded = false;
  unicity = true;
  showMessages = false;
  dbSource = new DBSource();

  constructor(private dbSourceService: DBSourceService,
    private store: Store<DBSourcesState>,
    public dialogRef: MatDialogRef<DbsourceModalComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
    if (this.data.dbsource) {
      this.dbSource = Object.assign({}, this.data.dbsource);
    }
  }

  checkConnection() {
    this.showMessages = true;
    this.loading = true;
    this.dbSourceService.checkConnection(this.dbSource).subscribe(res => {
      this.loading = false;
      this.dbSource.connectionSucceeded = res;
    }, err => {
      if (err.status === 406) {
        if (err.error === 4061) {
          this.dbSource.connectionSucceeded = false;
        }
      }
    });
  }

  checkUnicity() {
    this.dbSourceService.checkUnicity(this.dbSource.name, this.dbSource.id).subscribe(res => {
      if (res) {
        this.unicity = true;
      } else {
        this.unicity = false;
      }
    });
  }

  createNewDataSource() {

    this.dbSource.userEmail = localStorage.getItem('userEmail');
    this.dbSourceService.checkUnicity(this.dbSource.name, this.dbSource.id).subscribe(res => {
      if (this.dbSource.name && res) {
        if (!this.data.dbsource) {
          this.store.dispatch(new dbsourceActions.AddDBSource(this.dbSource));
        } else {
          this.store.dispatch(new dbsourceActions.UpdateDBSource(this.dbSource));
        }
        this.dialogRef.close();
      } else {
        this.unicity = false;
      }
    });

  }

  isStepCompleted(index) {
    if (index === 1) {
      if (this.dbSource.provider && this.dbSource.connectionMode) {
        return true;
      } else {
        return false;
      }
    }
    if (index === 2) {
      if (this.dbSource.connectionMode === 'FREE') {
        if (this.dbSource.name) {
          return true;
        } else {
          return false;
        }
      } else if (this.dbSource.connectionMode === 'CLOUD') {
        if (this.dbSource.name && this.dbSource.uri) {
          return true;
        } else {
          return false;
        }
      } else {
        if (this.dbSource.name && this.dbSource.host && this.dbSource.port) {
          if (this.dbSource.authRequired) {
            if (this.dbSource.username && this.dbSource.password && this.dbSource.authenticationDatabase) {
              return true;
            } else {
              return false;
            }
          }
          return true;
        }
        return false;
      }
    }
  }

}
