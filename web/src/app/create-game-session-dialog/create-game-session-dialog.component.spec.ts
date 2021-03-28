import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateGameSessionDialogComponent } from './create-game-session-dialog.component';

describe('CreateGameSessionDialogComponent', () => {
  let component: CreateGameSessionDialogComponent;
  let fixture: ComponentFixture<CreateGameSessionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateGameSessionDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateGameSessionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
