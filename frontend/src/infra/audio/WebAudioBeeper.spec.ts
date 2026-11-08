import { describe, expect, it } from "vitest";
import { WebAudioBeeper } from "./WebAudioBeeper";

describe("WebAudioBeeper", () => {
  it("plays the tone of the checkout", () => {
    // Given
    const browser = browserAudio();
    const beeper = new WebAudioBeeper(browser.newContext);
    browser.currentTime = 2;

    // When
    beeper.beep();

    // Then
    expect(browser.oscillators).toHaveLength(1);
    const [oscillator] = browser.oscillators;
    expect(oscillator!.frequency.value).toBe(1000);
    expect(oscillator!.wave!.real).toEqual([0, 0, 0, 0, 0, 0]);
    const expected = [0, 1, 0, 10 ** (-19 / 20), 0, 10 ** (-28 / 20)];
    expect(oscillator!.wave!.imag).toHaveLength(expected.length);
    expected.forEach((level, harmonic) =>
      expect(oscillator!.wave!.imag[harmonic]).toBeCloseTo(level),
    );
    expect(oscillator!.connectedTo).toEqual([browser.destination]);
    expect(oscillator!.startedAt).toBe(2);
    expect(oscillator!.stoppedAt).toBeCloseTo(2.099);
  });
});

interface StandInWave {
  real: number[];
  imag: number[];
}

interface StandInOscillator {
  frequency: { value: number };
  wave: StandInWave | null;
  connectedTo: unknown[];
  startedAt: number | null;
  stoppedAt: number | null;
  setPeriodicWave(wave: StandInWave): void;
  connect(node: unknown): void;
  start(when: number): void;
  stop(when: number): void;
}

function browserAudio() {
  const browser = {
    contexts: 0,
    resumes: 0,
    state: "running" as AudioContextState,
    currentTime: 0,
    destination: { name: "destination" },
    oscillators: [] as StandInOscillator[],
    newContext,
  };

  function newContext(): AudioContext {
    browser.contexts += 1;
    const context = {
      get state() {
        return browser.state;
      },
      get currentTime() {
        return browser.currentTime;
      },
      destination: browser.destination,
      resume: async () => {
        browser.resumes += 1;
        browser.state = "running";
      },
      createOscillator: () => {
        const oscillator = newOscillator();
        browser.oscillators.push(oscillator);
        return oscillator;
      },
      createPeriodicWave: (
        real: ArrayLike<number>,
        imag: ArrayLike<number>,
      ): StandInWave => ({ real: Array.from(real), imag: Array.from(imag) }),
    };
    return context as unknown as AudioContext;
  }

  return browser;
}

function newOscillator(): StandInOscillator {
  return {
    frequency: { value: 440 },
    wave: null,
    connectedTo: [],
    startedAt: null,
    stoppedAt: null,
    setPeriodicWave(wave) {
      this.wave = wave;
    },
    connect(node) {
      this.connectedTo.push(node);
    },
    start(when) {
      this.startedAt = when;
    },
    stop(when) {
      this.stoppedAt = when;
    },
  };
}
