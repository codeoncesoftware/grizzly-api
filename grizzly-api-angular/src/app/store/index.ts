import { ProjectEffects } from './project/project.effect';
import { ContainerEffects } from './container/container.effects';
import { DBSourceEffects } from './dbsource/dbsource.effect';
import { AuthEffects } from './authentication/auth.effects';
import { DashboardEffects } from './dashboard/dashboard.effect';

export const effects: any[] = [ProjectEffects, ContainerEffects, DBSourceEffects, AuthEffects, DashboardEffects];

