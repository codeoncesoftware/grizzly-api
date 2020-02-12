import * as dbsource from './dbsource.actions';
import * as globalActions from '../global.actions';
import { DBSourcesState } from './dbsource.state';
import { DBSource } from 'src/app/shared/models/DBSource';
import { Collection } from './dbsource';

export const initialDBSourcesState: DBSourcesState = {
    dbsources: [],
    active: new DBSource(),
    loading: false
};

export function dbsourceReducer(state = initialDBSourcesState, action: dbsource.DBSourceActions): DBSourcesState {
    switch (action.type) {

        case dbsource.ADD_DBSOURCE_SUCCESS:
            const newState = Object.assign({}, state);
            newState.dbsources.push(action.payload as DBSource);
            return newState;

        case dbsource.LOAD_ALL_DBSOURCES_SUCCESS:
            const newAllDBSourcesState = Object.assign({}, state);
            newAllDBSourcesState.dbsources = (action.payload as DBSource[]);
            newAllDBSourcesState.loading = false;
            return newAllDBSourcesState;

        case dbsource.UPDATE_DBSOURCE_SUCCESS:
            const newUpdatedState = Object.assign({}, state);
            const updatedDbsource = (action.payload as DBSource);
            if (!updatedDbsource.active) {
                updatedDbsource.collectionsList = [];
            }
            const indexDBSourceToUpdate = newUpdatedState.dbsources.findIndex(db => db.id === (action.payload as DBSource).id);
            newUpdatedState.dbsources[indexDBSourceToUpdate] = updatedDbsource;
            newUpdatedState.active = updatedDbsource;
            return newUpdatedState;

        case dbsource.LOAD_ACTIVE_DBSOURCE:
            const stateNoActive = Object.assign({}, state);
            stateNoActive.active = (action.payload as DBSource);
            return stateNoActive;

        case dbsource.LOAD_ALL_DBSOURCES:
            const stateBeforeLoad = Object.assign({}, state);
            stateBeforeLoad.loading = true;
            return stateBeforeLoad;

        case dbsource.DELETE_DBSOURCE_SUCCESS:
            const stateAfterDelete = Object.assign({}, state);
            stateAfterDelete.dbsources = stateAfterDelete.dbsources.filter((db) => db.id !== action.payload);
            if (stateAfterDelete.dbsources.length > 0) {
                stateAfterDelete.active = stateAfterDelete.dbsources[stateAfterDelete.dbsources.length - 1];
            } else {
                stateAfterDelete.active = null;
            }
            return stateAfterDelete;

        case dbsource.ADD_NEW_COLLECTION_SUCCESS:
            const stateAfterCollectionAdd = Object.assign({}, state);
            const dbsourceeIndex = stateAfterCollectionAdd.dbsources.findIndex(dbs => dbs.id === (action.payload as Collection).dbsourceId);
            const dbsourcee = stateAfterCollectionAdd.dbsources[dbsourceeIndex];
            if (dbsourcee) {
                const dbIndex = dbsourcee.databases.findIndex(database => database.name === (action as dbsource.AddNewCollectionSuccess).payload.databaseName);
                stateAfterCollectionAdd.dbsources[dbsourceeIndex].databases[dbIndex].collections.push((action as dbsource.AddNewCollectionSuccess).payload.collectionName);
            }
            return stateAfterCollectionAdd;

        case dbsource.DROP_COLLECTION_SUCCESS:
            const stateBeforeDrop = Object.assign({}, state);
            const dbsIndex = stateBeforeDrop.dbsources.findIndex(dbs => dbs.id === (action.payload as Collection).dbsourceId);
            const dbndex = stateBeforeDrop.dbsources[dbsIndex].databases.findIndex(db => db.name === (action.payload as Collection).databaseName);
            stateBeforeDrop.dbsources[dbsIndex].databases[dbndex].collections = stateBeforeDrop.dbsources[dbsIndex].databases[dbndex].collections.filter(coll => coll !== (action.payload as Collection).collectionName);
            stateBeforeDrop.active.databases[dbndex].collections = stateBeforeDrop.active.databases[dbndex].collections.filter(coll => coll !== (action.payload as Collection).collectionName);
            return stateBeforeDrop;

        case globalActions.EFFECT_ERROR:
            return Object.assign({}, state);

        default:
            return state;
    }
}
