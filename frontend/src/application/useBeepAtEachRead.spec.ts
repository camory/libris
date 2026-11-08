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
    const { tap, read } = useBeepAtEachRead(beeper);
    tap();
    tap();
    tap();

    // When
    read();

    // Then
    expect(beeper.beeps).toBe(2);
  });
});
