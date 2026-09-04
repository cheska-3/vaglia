import { Injectable, effect, signal } from '@angular/core';

const STORAGE_KEY = 'vaglia-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly isDark = signal<boolean>(this.resolveInitial());

  constructor() {
    effect(() => {
      const theme = this.isDark() ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', theme);
      try {
        localStorage.setItem(STORAGE_KEY, theme);
      } catch {
        // localStorage unavailable (private browsing, etc.) — theme just won't persist.
      }
    });
  }

  toggle(): void {
    this.isDark.set(!this.isDark());
  }

  private resolveInitial(): boolean {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === 'dark') return true;
      if (stored === 'light') return false;
    } catch {
      // ignore, fall through to system preference
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
  }
}
