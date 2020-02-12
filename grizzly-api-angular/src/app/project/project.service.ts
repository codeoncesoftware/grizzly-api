import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Project } from 'src/app/shared/models/Project';

import { Store } from '@ngrx/store';
import { ProjectsState } from '../store/project/project.state';


@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  token: string;

  baseUrl: string = environment.baseUrl + '/api/project';

  constructor(private store: Store<ProjectsState>, private http: HttpClient) {

  }

  /**
   * Check a Project's Name Unicity
   * @param name The Name to Check in Database
   */
  public checkProjectNameUnicity(name: string, id: string): Observable<boolean> {
    let param = '/check/' + name;
    if (id) {
      param = '/check/' + name + '/' + id;
    }
    return this.http.get<boolean>(this.baseUrl + param);
  }

  /**
   * Fetch All Projects to Display on the sidenav Bar
   */
  public getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.baseUrl + '/all/');
  }

  /**
   * Fetch a Project By it's Uuid
   * @param projectUid to Fetch the Project By Id
   */
  public getProjectByUid(projectUid: string): Observable<Project> {
    return this.http.get<Project>(this.baseUrl + '/' + projectUid);
  }

  /**
   * Save a new project from a filled form
   * @param obj Project Object with Details
   */
  public createProject(obj: Project): Observable<Project> {
    return this.http.post<Project>(this.baseUrl + '/create', obj);
  }

  /**
   * Update an existing project with new informations
   * @param proj The new Project's Details
   */
  public updateProject(proj: Project) {
    return this.http.put(this.baseUrl + '/update/' + proj.id, proj);
  }

  /**
   * Delete a project by it's uuid
   * @param id to Fetch the Project By Id
   */
  public deleteProjectById(id: string) {
    return this.http.delete(this.baseUrl + '/delete/' + id);
  }
}
