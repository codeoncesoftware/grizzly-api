import { Component, OnInit, AfterViewInit, AfterContentInit } from '@angular/core';
import { Project } from '../../shared/models/Project';
import { Store } from '@ngrx/store';
import { ActivatedRoute } from '@angular/router';
import { ProjectsState } from 'src/app/store/project/project.state';
import { MatDialog } from '@angular/material/dialog';
import { ProjectModalComponent } from '../project-modal/project-modal.component';
import { Container } from 'src/app/shared/models/Container';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent implements OnInit {

  public projectsList: Project[];
  project = new Project();

  constructor(public dialog: MatDialog, private store: Store<ProjectsState>
  ) {
  }

  ngOnInit() {

    // tslint:disable-next-line: no-string-literal
    this.store.select('projects').subscribe(state => this.projectsList = state['projects']);
    // tslint:disable-next-line: no-string-literal
  }

  // Open the modal with the create project form
  openDialog(): void {
    this.dialog.open(ProjectModalComponent, {
      // Modal configuration
      width: '50%',
      height: '75vh',
      position: {
        top: '15vh'
      },
      data: {
        project: this.project,
        action: {
          update: false,
          create: true,
          msg: 'popups.project.add'
        }
      },
    });
  }

}
