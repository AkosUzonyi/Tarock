import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { User } from './_models/dto';
import { AuthService } from './_services/auth.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'tarock-web';

  user$: Observable<User | null>;
  language: string;

  constructor(
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService,
  ) {
    this.user$ = authService.getUserObservable();
    this.translate.addLangs(['hu', 'en']);
    this.translate.setDefaultLang('hu');
    this.language = localStorage.getItem('language') || 'hu';
    this.updateLanguage();
  }

  ngOnInit() {
    this.authService.init();
  }

  updateLanguage() {
    this.translate.use(this.language);
    localStorage.setItem('language', this.language);
  }

  login() {
    if (!this.router.url.startsWith('/login'))
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }

  logout() {
    this.authService.logout();
  }
}
