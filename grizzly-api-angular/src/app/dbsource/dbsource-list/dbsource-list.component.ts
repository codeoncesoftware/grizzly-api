import { Component, OnInit } from '@angular/core';
import { DBSource } from 'src/app/shared/models/DBSource';
import { DBSourceService } from '../dbsource.service';
import { Store } from '@ngrx/store';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { DbsourceModalComponent } from '../dbsource-modal/dbsource-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { DbSourceModalService } from '../dbsource-modal/dbsource-modal.service';

@Component({
  selector: 'app-dbsource-list',
  templateUrl: './dbsource-list.component.html',
  styleUrls: ['./dbsource-list.component.scss']
})
export class DbsourceListComponent implements OnInit {

  dbSourceList: DBSource[] = [];
  fetching = false;
  dots = '.';

  constructor(public dialog: MatDialog, private dbsourceModalService: DbSourceModalService, private store: Store<DBSourcesState>) { }

  ngOnInit() {
    this.store.select('dbsources').subscribe((res: DBSource[]) => {
      // tslint:disable-next-line: no-string-literal
      this.dbSourceList = res['dbsources'];
      // tslint:disable-next-line: no-string-literal
      this.fetching = res['loading'];
      if (this.fetching === true) {
        this.displayFetchingDots();
      }
    });
  }

  public openDBSourceModal() {
    this.dbsourceModalService.openAdd(null);
  }

  public displayFetchingDots() {
    if (this.dots.length > 4) {
      this.dots = '.';
    } else {
      this.dots = this.dots + '.';
    }
    setTimeout(() => this.displayFetchingDots(), 1000);

    return this.dots;
  }
}
