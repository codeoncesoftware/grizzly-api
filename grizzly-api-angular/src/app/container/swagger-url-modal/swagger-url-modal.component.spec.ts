import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SwaggerUrlModalComponent } from './swagger-url-modal.component';

describe('SwaggerUrlModalComponent', () => {
  let component: SwaggerUrlModalComponent;
  let fixture: ComponentFixture<SwaggerUrlModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SwaggerUrlModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SwaggerUrlModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
