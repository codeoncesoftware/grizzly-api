import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { DBSource } from 'src/app/shared/models/DBSource';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';

@Component({
  selector: 'app-dbsource-modal-cloud-connection',
  templateUrl: './dbsource-modal-cloud-connection.component.html',
  styleUrls: ['./dbsource-modal-cloud-connection.component.scss']
})
export class DbsourceModalCloudConnectionComponent implements OnInit {
  isFr: boolean;
  selectedLanguage: string;
  @Input() dbSource: DBSource;
  @Output() checkConnection: EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  onCheckConnection() {
    this.checkConnection.emit(this.dbSource);
  }

}
