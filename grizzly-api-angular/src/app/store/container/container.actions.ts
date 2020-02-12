import { Action } from '@ngrx/store';
import { Container } from '../../shared/models/Container';
import { ResourceGroup } from 'src/app/shared/models/ResourceGroup';

export const LOAD_ALL_CONTAINERS_SUCCESS = '[Container] LOAD_ALL_CONTAINERS_SUCCESS';
export const LOAD_ALL_CONTAINERS = '[Container] LOAD_ALL_CONTAINERS';
export const ADD_CONTAINER = '[Container] ADD_CONTAINER';
export const CLONE_CONTAINER = '[Container] ADD_CONTAINER';
export const ADD_CONTAINER_SUCCESS = '[Container] ADD_CONTAINER_SUCCESS';
export const DELETE_CONTAINER = '[Container] REMOVE_CONTAINER';
export const DELETE_ACTIVE_CONTAINER_FILES = '[Container] DELETE_ACTIVE_CONTAINER_FILES';
export const DELETE_ACTIVE_CONTAINER_FILES_SUCCESS = '[Container] DELETE_ACTIVE_CONTAINER_FILES_SUCCESS';
export const UPDATE_CONTAINER = '[Container] UPDATE_CONTAINER';
export const UPDATE_CONTAINER_SUCCESS = '[Container] UPDATE_CONTAINER_SUCCESS';
export const DELETE_CONTAINER_SUCCESS = '[Container] REMOVE_CONTAINER_SUCCESS';
export const GET_CONTAINER = '[Container] GET_CONTAINER';
export const GET_CONTAINER_SUCCESS = '[Container] GET_CONTAINER_SUCCESS';
export const DELETE_ALL_CONTAINERS = '[Container] REMOVE_ALL_CONTAINERS';
export const DELETE_ALL_CONTAINERS_SUCCESS = '[Container] REMOVE_ALL_CONTAINERS_SUCCESS';
export const LOAD_ACTIVE_CONTAINER = '[Container] LOAD_ACTIVE_CONTAINER';
export const LOAD_ACTIVE_CONTAINER_BY_ID = '[Container] LOAD_ACTIVE_CONTAINER_BY_ID';
export const IMPORT_SWAGGER_FILE = '[Container] IMPORT_SWAGGER_FILE';
export const ENABLE_DISABLE_CONTAINER = '[Container] ENABLE_DISABLE_CONTAINER';
export const ENABLE_DISABLE_SUCCESS = '[Container] ENABLE_DISABLE_SUCCESS';
export const IMPORT_CONTAINER = '[Container] IMPORT_CONTAINER';

// Container Operations
export const ADD_RESOURCE_GROUP = '[Container] ADD_RESOURCE_GROUP';

export class AddContainer implements Action {
    readonly type: string = ADD_CONTAINER;
    constructor(public payload: Container) { }
}

export class CloneContainer implements Action {
    readonly type: string = CLONE_CONTAINER;
    constructor(public payload: string, public name: string) { }
}

export class AddContainerSuccess implements Action {
    readonly type: string = ADD_CONTAINER_SUCCESS;
    constructor(public payload: Container) { }
}

export class DeleteContainer implements Action {
    readonly type: string = DELETE_CONTAINER;
    constructor(public payload: string) { }
}

export class DeleteContainerSucess implements Action {
    readonly type: string = DELETE_CONTAINER_SUCCESS;
    constructor(public payload: string) { }
}


export class DeleteActiveContainerFiles implements Action {
    readonly type: string = DELETE_ACTIVE_CONTAINER_FILES;
    constructor(public payload: string) { }
}


export class DeleteActiveContainerFilesSuccess implements Action {
    readonly type: string = DELETE_ACTIVE_CONTAINER_FILES_SUCCESS;
    constructor(public payload: string) { }
}

export class UpdateContainer implements Action {
    readonly type: string = UPDATE_CONTAINER;
    constructor(public payload: Container, public message: string) { }
}

export class UpdateContainerSuccess implements Action {
    readonly type: string = UPDATE_CONTAINER_SUCCESS;
    constructor(public payload: Container) { }
}

export class LoadAllContainers implements Action {
    readonly type: string = LOAD_ALL_CONTAINERS;
    constructor(public payload: string) { }
}

export class LoadAllContainersSuccess implements Action {
    readonly type: string = LOAD_ALL_CONTAINERS_SUCCESS;
    constructor(public payload: Container[]) { }
}

export class GetContainer implements Action {
    readonly type = GET_CONTAINER;
    constructor(public payload: string, public msg: string) { }
}

export class DeleteAllContainers implements Action {
    readonly type: string = DELETE_ALL_CONTAINERS;
    constructor(public payload: string) { }
}

export class DeleteAllContainersSuccess implements Action {
    readonly type: string = DELETE_ALL_CONTAINERS_SUCCESS;
    constructor(public payload: any = {}) { }
}

export class LoadActiveContainer implements Action {
    readonly type: string = LOAD_ACTIVE_CONTAINER;
    constructor(public payload: Container) { }
}

export class LoadActiveContainerById implements Action {
    readonly type: string = LOAD_ACTIVE_CONTAINER_BY_ID;
    constructor(public payload: string) { }
}

export class ImportSwaggerFile implements Action {
    readonly type: string = IMPORT_SWAGGER_FILE;
    constructor(public payload: File, public projectId: string, public containerName: string) { }
}

export class ImportContainer implements Action {
    readonly type: string = IMPORT_CONTAINER;
    constructor(public payload: File, public projectId: string, public containerName: string, public dbsourceId: string, public databaseName: string) { }
}
export class EnableDisableContainer implements Action {
    readonly type: string = ENABLE_DISABLE_CONTAINER;
    constructor(public payload: string) {}
}

export class EnableDisableSuccess implements Action {
    readonly type: string = ENABLE_DISABLE_SUCCESS;
    constructor(public payload: string) {}
}

// Resource Group Actions
export class AddResourceGroup implements Action {
    readonly type: string = ADD_RESOURCE_GROUP;
    constructor(public id: string, public payload: ResourceGroup) { }
}

export type ContainerActions =
    AddResourceGroup |
    LoadAllContainers |
    LoadAllContainersSuccess |
    AddContainer |
    CloneContainer |
    AddContainerSuccess |
    DeleteContainer |
    DeleteContainerSucess |
    DeleteActiveContainerFiles |
    DeleteActiveContainerFilesSuccess |
    GetContainer |
    DeleteAllContainers |
    DeleteAllContainersSuccess |
    LoadActiveContainer |
    LoadActiveContainerById |
    EnableDisableContainer |
    EnableDisableSuccess |
    ImportSwaggerFile|
    ImportContainer;
