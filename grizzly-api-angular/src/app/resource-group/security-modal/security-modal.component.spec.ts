import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityModalComponent } from './security-modal.component';

describe('SecurityModalComponent', () => {
  let component: SecurityModalComponent;
  let fixture: ComponentFixture<SecurityModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurityModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
