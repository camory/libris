import type { Beeper } from "./Beeper";

const halfASecond = 500;

export function useBeepAtEachRead(beeper: Beeper): {
  beep: () => void;
  tap: (at: number) => void;
} {
  let taps = 0;
  let lastTap = -Infinity;
  let on = false;

  function beep() {
    if (on) {
      beeper.beep();
    }
  }

  function tap(at: number) {
    taps = at - lastTap < halfASecond ? taps + 1 : 1;
    lastTap = at;
    if (taps === 3) {
      on = !on;
      beep();
    }
  }

  return { beep, tap };
}
