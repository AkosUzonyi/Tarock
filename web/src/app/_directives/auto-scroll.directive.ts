import { Directive, ElementRef } from '@angular/core';

@Directive({
  selector: '[appAutoScroll]'
})
export class AutoScrollDirective {
  constructor(private el: ElementRef) {
  }

  ngAfterViewInit() {
    setTimeout(() => this.scrollToBottom(), 200);

    const observer = new MutationObserver(mutations => {
      this.scrollToBottom();
    });
    observer.observe(this.el.nativeElement, { childList: true });
  }

  scrollToBottom() {
    this.el.nativeElement.scrollTop = this.el.nativeElement.scrollHeight;
  }
}
