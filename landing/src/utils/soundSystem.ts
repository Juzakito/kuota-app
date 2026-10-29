/**
 * KUOTA — IDENTIDAD SONORA FINTECH (FASE 5)
 * Sistema de feedback acústico original sintetizado mediante Web Audio API.
 *
 * Reglas estrictas:
 * - Desactivado por defecto (`enabled = false`).
 * - Nunca reproduce sonido automáticamente al cargar la página.
 * - Nunca reproduce sonido en eventos hover.
 * - Incluye control de encendido/apagado y control de volumen (0.0 a 1.0).
 */

type SoundListener = (state: { enabled: boolean; volume: number }) => void;

class KuotaSoundEngine {
  private ctx: AudioContext | null = null;
  private enabled = false;
  private volume = 0.45;
  private listeners = new Set<SoundListener>();

  public getState(): { enabled: boolean; volume: number } {
    return { enabled: this.enabled, volume: this.volume };
  }

  public subscribe(listener: SoundListener): () => void {
    this.listeners.add(listener);
    listener(this.getState());
    return () => {
      this.listeners.delete(listener);
    };
  }

  private notify(): void {
    const snapshot = this.getState();
    this.listeners.forEach((cb) => cb(snapshot));
  }

  private ensureContext(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    if (!this.ctx) {
      const AudioCtx =
        window.AudioContext ||
        (window as unknown as { webkitAudioContext?: typeof AudioContext })
          .webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume().catch(() => {});
    }
    return this.ctx;
  }

  public setEnabled(next: boolean): void {
    this.enabled = next;
    if (next) {
      this.ensureContext();
      this.playSelectOption();
    }
    this.notify();
  }

  public setVolume(nextVolume: number): void {
    this.volume = Math.max(0, Math.min(1, nextVolume));
    this.notify();
  }

  private playTone(
    freq: number,
    startTimeOffset: number,
    duration: number,
    type: OscillatorType = 'sine',
    gainMultiplier = 0.18
  ): void {
    if (!this.enabled || this.volume <= 0.01) return;
    const ctx = this.ensureContext();
    if (!ctx) return;

    const now = ctx.currentTime + startTimeOffset;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = type;
    osc.frequency.setValueAtTime(freq, now);

    const peakGain = Math.max(0.001, this.volume * gainMultiplier);
    gain.gain.setValueAtTime(0.0001, now);
    gain.gain.exponentialRampToValueAtTime(peakGain, now + 0.012);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + duration);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start(now);
    osc.stop(now + duration + 0.01);
  }

  /** Evento 1: Click en CTA primario o secundario */
  public playCtaClick(): void {
    this.playTone(523.25, 0, 0.07, 'sine', 0.16); // C5
    this.playTone(659.25, 0.045, 0.11, 'sine', 0.18); // E5
  }

  /** Evento 2: Selección de pestaña, chip, nivel o paso */
  public playSelectOption(): void {
    this.playTone(587.33, 0, 0.055, 'triangle', 0.12); // D5
  }

  /** Evento 3: Confirmación de operación en el simulador de kuotas */
  public playConfirmOperation(): void {
    this.playTone(523.25, 0, 0.08, 'sine', 0.15); // C5
    this.playTone(659.25, 0.06, 0.09, 'sine', 0.16); // E5
    this.playTone(783.99, 0.12, 0.14, 'sine', 0.18); // G5
  }

  /** Evento 4: Finalización de demostración interactiva */
  public playDemoComplete(): void {
    this.playTone(659.25, 0, 0.09, 'sine', 0.14); // E5
    this.playTone(987.77, 0.07, 0.18, 'sine', 0.16); // B5
  }

  /** Evento 5: Alerta o límite alcanzado en formulario */
  public playFormError(): void {
    this.playTone(246.94, 0, 0.08, 'triangle', 0.14);
    this.playTone(220.0, 0.07, 0.1, 'triangle', 0.14);
  }
}

export const kuotaSound = new KuotaSoundEngine();
