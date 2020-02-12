import * as container from './container.actions';
import * as globalActions from '../global.actions';
import { Container } from 'src/app/shared/models/Container';
import { ContainerState } from './container.state';

export const initialContainerState: ContainerState = {
    containers: [],
    active: new Container(),
    success: false
};

export function containerReducer(state = initialContainerState, action: container.ContainerActions): ContainerState {
    switch (action.type) {

        case container.LOAD_ALL_CONTAINERS_SUCCESS:
            const oldState = Object.assign({}, state);
            oldState.containers = (action.payload as Container[]);
            return oldState;

        case container.ADD_CONTAINER_SUCCESS:
            const newState = Object.assign({}, state);
            newState.containers.push(action.payload as Container);
            newState.active = (action.payload as Container);
            return newState;

        case container.UPDATE_CONTAINER_SUCCESS:
            const stateUpdated = Object.assign({}, state);
            const index = stateUpdated.containers.findIndex(x => x.id === (action.payload as Container).id);
            stateUpdated.containers[index] = (action.payload as Container);
            stateUpdated.active = (action.payload as Container);
            return stateUpdated;

        case container.DELETE_CONTAINER_SUCCESS:
            const stateToReturn = Object.assign({}, state);
            stateToReturn.active = new Container();
            stateToReturn.containers = stateToReturn.containers.filter((item) => item.id !== (action.payload as string));
            return stateToReturn;

        case container.DELETE_ACTIVE_CONTAINER_FILES_SUCCESS:
            const stateToReturnNoFiles = Object.assign({}, state);
            const activeIndex = stateToReturnNoFiles.containers.findIndex(x => x.id === action.payload);
            const active = stateToReturnNoFiles.containers[activeIndex];
            active.hierarchy = null;
            stateToReturnNoFiles.active = active;
            const newConainersArray = Object.assign([], stateToReturnNoFiles.containers);
            newConainersArray.forEach(cont => {
                if (cont.id === action.payload) {
                    cont.hierarchy = null;
                }
            });
            stateToReturnNoFiles.containers = newConainersArray;
            return stateToReturnNoFiles;

        case container.DELETE_ALL_CONTAINERS_SUCCESS:
            const deleteAllState = Object.assign({}, state);
            deleteAllState.active = new Container();
            deleteAllState.containers = [];
            return deleteAllState;

        case container.LOAD_ACTIVE_CONTAINER:
            const activeState = Object.assign({}, state);
            activeState.active = (action.payload as Container);
            return activeState;

        case container.ENABLE_DISABLE_SUCCESS:
            const lastState = Object.assign({}, state);
            lastState.containers.filter(cont => cont.id !== (action.payload as string)).forEach(cont => cont.enabled = false);
            lastState.active.enabled = !lastState.active.enabled;
            return lastState;

        case container.LOAD_ACTIVE_CONTAINER_BY_ID:
            const stateBeforeActiveChange = Object.assign({}, state);
            stateBeforeActiveChange.active = stateBeforeActiveChange.containers.find(cont => cont.id === action.payload);
            return stateBeforeActiveChange;

        case globalActions.EFFECT_ERROR:
            return Object.assign({}, state);

        default:
            return state;
    }
}
