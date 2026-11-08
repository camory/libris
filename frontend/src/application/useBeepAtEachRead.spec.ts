import { describe, expect, it } from "vitest";
import { FakeBeeper } from "../fixture/FakeBeeper";
import { useBeepAtEachRead } from "./useBeepAtEachRead";

describe("useBeepAtEachRead", () => {
  it("turns the beep on at the third tap, sounding it", () => {
    // Given
    const beeper = new FakeBeeper();
    const { tap } = useBeepAtEachRead(beeper);

    // When
    tap(0);
    tap(100);
    const afterTwo = beeper.beeps;
    tap(200);

    // Then
    expect(afterTwo).toBe(0);
    expect(beeper.beeps).toBe(1);
  });

  it("starts a new row at a slow tap, counting it as the first", () => {
    // Given
    const beeper = new FakeBeeper();
    const { tap } = useBeepAtEachRead(beeper);

    // When
    tap(0);
    tap(100);
    tap(700);
    tap(800);
    const afterFour = beeper.beeps;
    tap(900);

    // Then
    expect(afterFour).toBe(0);
    expect(beeper.beeps).toBe(1);
  });

  it("sounds the beep at a read once it is on", () => {
    // Given
    const beeper = new FakeBeeper();
    const { beep, tap } = useBeepAtEachRead(beeper);
    tap(0);
    tap(100);
    tap(200);

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
