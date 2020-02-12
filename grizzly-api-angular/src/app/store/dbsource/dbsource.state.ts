import { DBSource } from 'src/app/shared/models/DBSource';

export interface DBSourcesState {
    dbsources: DBSource[];
    active: DBSource;
    loading: boolean;
}
