import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { DBSource } from 'src/app/shared/models/DBSource';

@Component({
  selector: 'app-dbsource-modal-direct-connection',
  templateUrl: './dbsource-modal-direct-connection.component.html',
  styleUrls: ['./dbsource-modal-direct-connection.component.scss']
})
export class DbsourceModalDirectConnectionComponent implements OnInit {

  @Input() dbSource: DBSource;
  @Output() checkConnection: EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  onCheckConnection() {
    this.checkConnection.emit(this.dbSource);
  }

  isCompleted() {
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
