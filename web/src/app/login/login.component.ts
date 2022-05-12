import { Component, OnDestroy, OnInit } from '@angular/core';
import {  ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../_services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  private userSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit() {
    this.authService.logout();

    this.userSubscription = this.authService.getUserObservable().subscribe(user => {
      if (user !== null)
        this.navigateBack();
    });

    this.authService.apiLogin();
  }

  navigateBack() {
    const returnUrl = this.activatedRoute.snapshot.queryParamMap.get('returnUrl') || '/';
    this.router.navigateByUrl(returnUrl, { replaceUrl: true });
  }

  ngOnDestroy() {
    this.userSubscription?.unsubscribe();
  }

  loginFacebook() {
    this.authService.loginFacebook();
  }

  loginGoogle() {
    this.authService.loginGoogle();
  }
}
