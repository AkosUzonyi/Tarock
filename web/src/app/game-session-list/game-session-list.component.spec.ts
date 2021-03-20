import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GameSessionListComponent } from './game-session-list.component';

describe('GameSessionListComponent', () => {
  let component: GameSessionListComponent;
  let fixture: ComponentFixture<GameSessionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GameSessionListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GameSessionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
