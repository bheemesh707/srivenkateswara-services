import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  showAbout = false;

  toggleAbout(event: Event) {
    event.preventDefault();
    this.showAbout = !this.showAbout;
  }
}
