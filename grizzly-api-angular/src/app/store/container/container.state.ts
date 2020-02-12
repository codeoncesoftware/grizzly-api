import { Container } from '../../shared/models/Container';

export interface ContainerState {
    containers: Container[];
    active: Container;
    success: boolean;
}
