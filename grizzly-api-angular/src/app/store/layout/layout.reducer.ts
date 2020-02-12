import { LayoutState } from './layout.state';
import * as actions from './layout.actions';
import { ResourceGroupLayout } from './layout.models';

export const initialLayoutState: LayoutState = {
    containerGroups: []
};

export function layoutReducer(state = initialLayoutState, action: actions.LayoutActions): LayoutState {
    if (action.type === actions.TOGGLE_RG) {

        const oldOpenState = Object.assign({}, state);
        const index = oldOpenState.containerGroups.findIndex(rg => rg.containerId === action.containerId);

        if (index !== -1) {
            if (!oldOpenState.containerGroups[index].openGroupsIndexs.includes(action.rgIndex)) {
                oldOpenState.containerGroups[index].openGroupsIndexs.push(action.rgIndex);
            } else {
                oldOpenState.containerGroups[index].openGroupsIndexs = oldOpenState.containerGroups[index].openGroupsIndexs.filter(i => i !== action.rgIndex);
            }
        } else {
            const obj = new ResourceGroupLayout(action.containerId, action.rgIndex);
            oldOpenState.containerGroups.push(obj);
        }

        return oldOpenState;
    } else {
        return Object.assign({}, state);
    }
}

