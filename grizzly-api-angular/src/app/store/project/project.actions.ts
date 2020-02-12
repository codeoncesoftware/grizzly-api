import { Action } from '@ngrx/store';
import { Project } from '../../shared/models/Project';

export const LOAD_ALL_PROJECTS_SUCCESS = '[Project] LOAD_ALL_PROJECTS_SUCCESS';
export const LOAD_ALL_PROJECTS = '[Project] LOAD_ALL_PROJECTS';
export const ADD_PROJECT = '[Project] ADD_PROJECT';
export const ADD_PROJECT_SUCCESS = '[Project] ADD_PROJECT_SUCCESS';
export const DELETE_PROJECT = '[Project] REMOVE_PROJECT';
export const UPDATE_PROJECT = '[Project] UPDATE_PROJECT';
export const UPDATE_PROJECT_SUCCESS = '[Project] UPDATE_PROJECT_SUCCESS';
export const DELETE_PROJECT_SUCCESS = '[Project] REMOVE_PROJECT_SUCCESS';
export const GET_PROJECT = '[Project] GET_PROJECT';
export const GET_PROJECT_SUCCESS = '[Project] GET_PROJECT_SUCCESS';

export class AddProject implements Action {
    readonly type: string = ADD_PROJECT;
    constructor(public payload: Project) {}
}

export class AddProjectSuccess implements Action {
    readonly type: string = ADD_PROJECT_SUCCESS;
    constructor(public payload: Project) {}
}

export class DeleteProject implements Action {
    readonly type: string = DELETE_PROJECT;
    constructor(public payload: string) {}
}

export class DeleteProjectSucess implements Action {
    readonly type: string = DELETE_PROJECT_SUCCESS;
    constructor(public payload: string) {}
}

export class UpdateProject implements Action {
    readonly type: string = UPDATE_PROJECT;
    constructor(public payload: Project) {}
}

export class UpdateProjectSuccess implements Action {
    readonly type: string = UPDATE_PROJECT_SUCCESS;
    constructor(public payload: Project) {}
}

export class LoadAllProjects implements Action {
    readonly type: string = LOAD_ALL_PROJECTS;
    constructor(public payload: any = {}) { }
}

export class LoadAllProjectsSuccess implements Action {
    readonly type: string = LOAD_ALL_PROJECTS_SUCCESS;
    constructor(public payload: Project[]) {}
}

export class GetProject implements Action {
    readonly type = GET_PROJECT;
    constructor(public payload: Project) {}
}

export type ProjectActions = LoadAllProjects | AddProject | AddProjectSuccess | DeleteProject | DeleteProjectSucess | LoadAllProjectsSuccess | GetProject;
