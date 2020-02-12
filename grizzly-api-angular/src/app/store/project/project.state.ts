import { Project } from '../../shared/models/Project';

export interface ProjectsState {
    projects: Project[];
    active: Project;
    success: boolean;
}
