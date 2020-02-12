import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { ResourceGroup } from '../shared/models/ResourceGroup';
import { MatDialog } from '@angular/material';
import { SecurityModalComponent } from './security-modal/security-modal.component';

@Injectable({
  providedIn: 'root'
})
export class ResourceGroupService {

  // à supprimer
  baseUrl: string = environment.baseUrl;

  constructor( private http: HttpClient , public dialog: MatDialog) { }

  public addResourceGroup(group: ResourceGroup): Observable<ResourceGroup> {
    return this.http.post<ResourceGroup>(this.baseUrl + '/group/create', group, {responseType: 'json'});
  }

  public getResourceGroupsByContainer(uuid: string): Observable<ResourceGroup[]> {
    return this.http.get<ResourceGroup[]>(this.baseUrl + '/group/getGroupsByContainer/' + uuid);
  }

  public deleteGroupByUuid(uuid: string) {
    return this.http.delete(this.baseUrl + '/group/delete/' + uuid);
  }


}
