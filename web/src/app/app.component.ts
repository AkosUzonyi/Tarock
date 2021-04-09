import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { User } from './_models/game-objects';
import { AuthService } from './_services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'tarock-web';

  user$: Observable<User | null>;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
    this.user$ = authService.getUserObservable();
  }

  ngOnInit() {
    this.authService.init();
  }

  login() {
    if (!this.router.url.startsWith('/login'))
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }

  logout() {
    this.authService.logout();
  }
}
