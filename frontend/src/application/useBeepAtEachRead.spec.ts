import { describe, expect, it } from "vitest";
import { FakeBeeper } from "../fixture/FakeBeeper";
import { useBeepAtEachRead } from "./useBeepAtEachRead";

describe("useBeepAtEachRead", () => {
  it("turns the beep on at the third tap, sounding it", () => {
    // Given
    const beeper = new FakeBeeper();
    const { tap } = useBeepAtEachRead(beeper);

    // When
    tap();
    tap();
    const afterTwo = beeper.beeps;
    tap();

    // Then
    expect(afterTwo).toBe(0);
    expect(beeper.beeps).toBe(1);
  });

  it("sounds the beep at a read once it is on", () => {
    // Given
    const beeper = new FakeBeeper();
    const { beep, tap } = useBeepAtEachRead(beeper);
    tap();
    tap();
    tap();

    // When
    beep();

    // Then
    expect(beeper.beeps).toBe(2);
  });

  it("keeps a read silent while the beep is off", () => {
    // Given
    const beeper = new FakeBeeper();
    const { beep } = useBeepAtEachRead(beeper);

    // When
    beep();

    // Then
    expect(beeper.beeps).toBe(0);
  });
});
