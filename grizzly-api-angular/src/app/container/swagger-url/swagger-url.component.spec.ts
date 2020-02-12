import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SwaggerUrlComponent } from './swagger-url.component';

describe('SwaggerUrlComponent', () => {
  let component: SwaggerUrlComponent;
  let fixture: ComponentFixture<SwaggerUrlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SwaggerUrlComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SwaggerUrlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
