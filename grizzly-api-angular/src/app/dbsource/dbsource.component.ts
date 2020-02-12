import { Component, OnInit } from '@angular/core';
import { DbsourceModalComponent } from './dbsource-modal/dbsource-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { AppTranslateService } from 'src/app/shared/services/app-translate-service';

@Component({
  selector: 'app-dbsource',
  templateUrl: './dbsource.component.html',
  styleUrls: ['./dbsource.component.scss']
})
export class DbsourceComponent implements OnInit {
  isFr: boolean;
  selectedLanguage: string;
  constructor() { }

  ngOnInit() {
  }

}
