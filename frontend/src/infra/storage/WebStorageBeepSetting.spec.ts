import { afterEach, describe, expect, it } from "vitest";
import { WebStorageBeepSetting } from "./WebStorageBeepSetting";

describe("WebStorageBeepSetting", () => {
  afterEach(() => {
    localStorage.clear();
  });

  it("keeps the beep on in the phone's storage", () => {
    // Given
    const beepSetting = new WebStorageBeepSetting(localStorage);

    // When
    beepSetting.keep(true);

    // Then
    expect(localStorage.getItem("beep")).toBe("on");
  });

  it("is off on a phone where the trick was never done", () => {
    // Given
    const beepSetting = new WebStorageBeepSetting(localStorage);

    // When
    const on = beepSetting.isOn();

    // Then
    expect(on).toBe(false);
  });

  it("reads the beep on from the phone's storage", () => {
    // Given
    localStorage.setItem("beep", "on");
    const beepSetting = new WebStorageBeepSetting(localStorage);

    // When
    const on = beepSetting.isOn();

    // Then
    expect(on).toBe(true);
  });

  it("forgets the beep turned off", () => {
    // Given
    localStorage.setItem("beep", "on");
    const beepSetting = new WebStorageBeepSetting(localStorage);

    // When
    beepSetting.keep(false);

    // Then
    expect(localStorage.getItem("beep")).toBeNull();
  });
});
