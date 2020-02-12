import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ResourceGroup } from 'src/app/shared/models/ResourceGroup';
import { Container } from 'src/app/shared/models/Container';
import { ContainerState } from 'src/app/store/container/container.state';
import { Store } from '@ngrx/store';
import * as containerActions from '../../store/container/container.actions';
import * as layoutActions from '../../store/layout/layout.actions';
import { ToastrService } from 'ngx-toastr';
import { LayoutState } from 'src/app/store/layout/layout.state';

@Component({
  selector: 'app-resource-group-modal',
  templateUrl: './resource-group-modal.component.html',
  styleUrls: ['./resource-group-modal.component.scss']
})
export class ResourceGroupModalComponent implements OnInit {
  constructor(private layoutStore: Store<LayoutState>,
              private toaster: ToastrService,
              public dialogRef: MatDialogRef<ResourceGroupModalComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private store: Store<ContainerState>) { }
  unique = true;
  container = new Container();
  containerToSave = new Container();
  resourceGroup = new ResourceGroup();
  disabled: false;

  ngOnInit() {
    this.resourceGroup = { ...this.data.resourceGroup };
    this.container = { ...this.data.container };
  }

  onInput() {
    this.unique = true;
  }

  getGroupIndex(group: ResourceGroup) {
    return this.container.resourceGroups.findIndex((x: ResourceGroup) => x.name.toUpperCase() === group.name.toUpperCase());
  }

  validateGroup(group: ResourceGroup) {

    this.containerToSave = JSON.parse(JSON.stringify(this.container));
    const groupIndex = this.getGroupIndex(group);
    let msg = '';

    if (this.data.action.create) { // create
      if (groupIndex < 0) {
        this.unique = true;
        this.containerToSave.resourceGroups.push(group);
        msg = 'Group Added';
      } else {
        this.unique = false;
      }
      // Open Group
      this.layoutStore.dispatch(new layoutActions.ToggleRg(this.containerToSave.id, this.containerToSave.resourceGroups.length - 1));
    } else { // update
      if (groupIndex < 0 || group.name === this.data.resourceGroup.name) {
        this.unique = true;
        const oldIndex = this.getGroupIndex(this.data.resourceGroup);
        this.containerToSave.resourceGroups[oldIndex] = group;
        msg = 'Group Updated';
      } else {
        this.unique = false;
      }
    }
    if (this.unique) {
      this.containerToSave.hierarchy = 'none';
      this.store.dispatch(new containerActions.UpdateContainer(this.containerToSave, msg));
      this.dialogRef.close(true);
    }
  }
}
