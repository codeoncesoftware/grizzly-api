import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MatDialog, MAT_DIALOG_DATA } from '@angular/material';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ProjectsState } from '../../store/project/project.state';

import * as project from '../../store/project/project.actions';
@Component({
  selector: 'app-role-modal',
  templateUrl: './role-modal.component.html',
  styleUrls: ['./role-modal.component.sass']
})
export class RoleModalComponent implements OnInit {

  role: string;
  oldRoles: string[];
  modified: boolean;
  unique = true;
  constructor(  public dialogRef: MatDialogRef<RoleModalComponent>,
                public dialog: MatDialog,
                @Inject(MAT_DIALOG_DATA) public data: any,
                private store: Store<ProjectsState>) { }

  ngOnInit() {
    this.oldRoles = Object.assign([], this.data.project.roles);
  }

   /** ADD New Role */
   public addNewRole() {
     this.unique = true;
     if (this.role) {
      this.data.project.roles.forEach(element => {
        if (this.role === element) {
          this.unique = false;
        }
      });
      if (this.unique) {
        this.data.project.roles.push(this.role);

      }
      this.modified = true;

    }
  }
  /** DELETE Role with a click */
  public deleteRole(i: number) {
    this.modified = true;
    this.data.project.roles.splice(i, 1);
  }

  confirmChoice() {
    this.store.dispatch(new project.UpdateProject(this.data.project));
    this.dialogRef.close();
  }

  closeRoles() {
    this.data.project.roles = this.oldRoles;
    this.dialogRef.close();

  }


}
