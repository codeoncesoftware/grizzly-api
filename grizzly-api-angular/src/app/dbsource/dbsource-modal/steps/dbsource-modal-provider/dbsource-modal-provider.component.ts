import { Component, OnInit, Input } from '@angular/core';
import { DBSource } from 'src/app/shared/models/DBSource';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';
@Component({
  selector: 'app-dbsource-modal-provider',
  templateUrl: './dbsource-modal-provider.component.html',
  styleUrls: ['./dbsource-modal-provider.component.scss']
})
export class DbsourceModalProviderComponent implements OnInit {

  @Input() dbSource: DBSource;

  constructor() { }

  ngOnInit() {
  }

}
