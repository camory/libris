import type { Beeper } from "./Beeper";

export function useBeepAtEachRead(beeper: Beeper): {
  tap: () => void;
  read: () => void;
} {
  let taps = 0;

  function tap() {
    taps += 1;
    if (taps === 3) {
      beeper.beep();
    }
  }

  function read() {
    beeper.beep();
  }

  return { tap, read };
}
