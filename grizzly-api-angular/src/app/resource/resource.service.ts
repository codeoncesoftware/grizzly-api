import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Resource } from '../shared/models/Resource';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
/**
 * This Service Handles all resources Operations (CRUD)
 */
export class ResourceService {

  constructor(private http: HttpClient) { }

  baseUrl: string = environment.baseUrl;

  public saveResource(resource: Resource) {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };
    return this.http.post<Resource>(this.baseUrl + '/api/resource/create', resource, httpOptions);
  }

  public getResourcesByGroup(groupUuid: string): Observable<Resource[]> {
    return this.http.get<Resource[]>(this.baseUrl + '/api/resource/getResourcesByGroup/' + groupUuid);
  }

  public deleteResourceByUuid(uuid: string) {
    return this.http.delete(this.baseUrl + '/api/resource/delete/' + uuid);
  }

  /** Save a File in Grid Fs (This is not used for NOW, it was abandoned) */
  public saveFile(resource: File, containerId: string) {
    const formData: FormData = new FormData();
    formData.append('file', resource);
    formData.append('containerId', containerId);
    return this.http.post(this.baseUrl + '/api/resource/uploadFile', formData);
  }

  /** Delete a File from GridFs based on it's Id */
  public deleteFile(containerId: string, idFile: string) {
    return this.http.delete(this.baseUrl + '/api/resource/delete/' + containerId + '/' + idFile);
  }

  /** Import a Remote Git Repository and get it's hierarchy */
  public importGitRepository(gitRepoUrl: string, branch: string, containerId: string, dbsourceId: string, databaseName: string, gitUsername: string, gitPassword: string) {
    return this.http.post(this.baseUrl + '/api/resource/importGitProject', {gitRepoUrl, branch, containerId, dbsourceId, databaseName, gitUsername, gitPassword }, { params: {} });
  }

  /** Send a ZIP File to the server to parse and get it's hierarchy */
  public importZipFile(zipFile: File, idContainer: string, dbsourceId: string, databaseName: string): Observable<string> {
    const formData: FormData = new FormData();
    formData.append('zipFile', zipFile);
    formData.append('idContainer', idContainer);
    formData.append('dbsourceId', dbsourceId);
    formData.append('databaseName', databaseName);
    return this.http.post<string>(this.baseUrl + '/api/resource/importZipProject', formData);
  }

  /** Get List of all the branch for a given repository url */
  public getBranchsList(gitRepoUrl: string, gitUsername: string, gitPassword: string) {
    return this.http.post(this.baseUrl + '/api/resource/getbranchslist', { gitRepoUrl,  gitUsername, gitPassword}, { params: { }, headers: new HttpHeaders().set('Content-Type', 'application/json') });
  }

  /**
   * Execute API Call
   * @param containerId To Fetch The Container
   * @param path of The API To Execute
   * @param executionType to Distinguish the API
   */
  public execute(containerId, path, executionType) {
    return this.http.post(this.baseUrl + '/runtime/' + containerId + path, {}, { responseType: 'text' });
  }

  /**
   * Delete every file related to the given container ID
   *
   * @param containerId to be used for files metadate fetch
   */
  public deleteFiles(containerId: string) {
    return this.http.delete(this.baseUrl + '/api/resource/delete/' + containerId);
  }

}
