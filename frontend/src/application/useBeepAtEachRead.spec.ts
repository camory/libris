import { describe, expect, it } from "vitest";
import { FakeBeepSetting } from "../fixture/FakeBeepSetting";
import { FakeBeeper } from "../fixture/FakeBeeper";
import { useBeepAtEachRead } from "./useBeepAtEachRead";

describe("useBeepAtEachRead", () => {
  it("turns the beep on at the third tap, sounding it", () => {
    // Given
    const beeper = new FakeBeeper();
    const { tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());

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
    const { tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());

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

  it("counts a tap less than half a second after the one before", () => {
    // Given
    const beeper = new FakeBeeper();
    const { tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());

    // When
    tap(0);
    tap(499);
    tap(998);

    // Then
    expect(beeper.beeps).toBe(1);
  });

  it("changes nothing with three taps half a second apart", () => {
    // Given
    const beeper = new FakeBeeper();
    const { tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());

    // When
    tap(0);
    tap(500);
    tap(1000);

    // Then
    expect(beeper.beeps).toBe(0);
  });

  it("sounds the beep at a read once it is on", () => {
    // Given
    const beeper = new FakeBeeper();
    const { beep, tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());
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
    const { beep } = useBeepAtEachRead(beeper, new FakeBeepSetting());

    // When
    beep();

    // Then
    expect(beeper.beeps).toBe(0);
  });

  it("turns the beep off at the trick done again, in silence", () => {
    // Given
    const beeper = new FakeBeeper();
    const { beep, tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());
    tap(0);
    tap(100);
    tap(200);

    // When
    tap(1000);
    tap(1100);
    tap(1200);
    const afterTheTrick = beeper.beeps;
    beep();

    // Then
    expect(afterTheTrick).toBe(1);
    expect(beeper.beeps).toBe(1);
  });

  it("starts a new row once the trick has worked", () => {
    // Given
    const beeper = new FakeBeeper();
    const { beep, tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());

    // When
    tap(0);
    tap(100);
    tap(200);
    tap(300);
    tap(400);
    tap(500);
    beep();

    // Then
    expect(beeper.beeps).toBe(1);
  });

  it("turns the beep on again at a third row, sounding it", () => {
    // Given
    const beeper = new FakeBeeper();
    const { tap } = useBeepAtEachRead(beeper, new FakeBeepSetting());

    // When
    tap(0);
    tap(100);
    tap(200);
    tap(300);
    tap(400);
    tap(500);
    tap(600);
    tap(700);
    tap(800);

    // Then
    expect(beeper.beeps).toBe(2);
  });

  it("starts with the beep on where the phone kept it on", () => {
    // Given
    const beeper = new FakeBeeper();
    const { beep } = useBeepAtEachRead(beeper, new FakeBeepSetting(true));

    // When
    beep();

    // Then
    expect(beeper.beeps).toBe(1);
  });

  it("keeps the beep on, turned on by the trick", () => {
    // Given
    const beepSetting = new FakeBeepSetting();
    const { tap } = useBeepAtEachRead(new FakeBeeper(), beepSetting);

    // When
    tap(0);
    tap(100);
    tap(200);

    // Then
    expect(beepSetting.on).toBe(true);
  });
});
