import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecuteModalComponent } from './execute-modal.component';

describe('ExecuteModalComponent', () => {
  let component: ExecuteModalComponent;
  let fixture: ComponentFixture<ExecuteModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExecuteModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecuteModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
