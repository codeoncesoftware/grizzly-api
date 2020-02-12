import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DbsourceModalReviewComponent } from './dbsource-modal-review.component';

describe('DbsourceModalReviewComponent', () => {
  let component: DbsourceModalReviewComponent;
  let fixture: ComponentFixture<DbsourceModalReviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DbsourceModalReviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbsourceModalReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
