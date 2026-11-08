import type { Beeper } from "../../application/Beeper";

const frequency = 1000;
const duration = 0.099;
const partials = [
  { harmonic: 1, decibels: 0 },
  { harmonic: 3, decibels: -19 },
  { harmonic: 5, decibels: -28 },
];

export class WebAudioBeeper implements Beeper {
  constructor(private readonly newContext: () => AudioContext) {}

  beep(): void {
    const context = this.newContext();
    const oscillator = context.createOscillator();
    oscillator.frequency.value = frequency;
    oscillator.setPeriodicWave(toneOf(context));
    oscillator.connect(context.destination);
    oscillator.start(context.currentTime);
    oscillator.stop(context.currentTime + duration);
  }
}

function toneOf(context: AudioContext): PeriodicWave {
  const size = Math.max(...partials.map(({ harmonic }) => harmonic)) + 1;
  const real = new Float32Array(size);
  const imag = new Float32Array(size);
  for (const { harmonic, decibels } of partials) {
    imag[harmonic] = 10 ** (decibels / 20);
  }
  return context.createPeriodicWave(real, imag);
}
