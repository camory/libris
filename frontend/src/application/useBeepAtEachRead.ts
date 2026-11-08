import type { Beeper } from "./Beeper";

export function useBeepAtEachRead(beeper: Beeper): {
  beep: () => void;
  tap: () => void;
} {
  let taps = 0;
  let on = false;

  function beep() {
    if (on) {
      beeper.beep();
    }
  }

  function tap() {
    taps += 1;
    if (taps === 3) {
      on = true;
      beeper.beep();
    }
  }

  return { beep, tap };
}
