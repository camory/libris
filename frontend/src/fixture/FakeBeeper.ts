import type { Beeper } from "../application/Beeper";

export class FakeBeeper implements Beeper {
  beeps = 0;

  beep(): void {
    this.beeps += 1;
  }

  ready(): void {}
}
