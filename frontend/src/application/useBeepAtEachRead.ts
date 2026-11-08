import type { Beeper } from "./Beeper";

export function useBeepAtEachRead(beeper: Beeper): {
  tap: () => void;
  read: () => void;
} {
  let taps = 0;
  let on = false;

  function tap() {
    taps += 1;
    if (taps === 3) {
      on = true;
      beeper.beep();
    }
  }

  function read() {
    if (on) {
      beeper.beep();
    }
  }

  return { tap, read };
}
