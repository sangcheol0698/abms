declare module 'nprogress' {
  interface NProgressOptions {
    showSpinner?: boolean;
    minimum?: number;
    easing?: string;
    speed?: number;
  }

  interface NProgressStatic {
    configure(options: NProgressOptions): void;
    isStarted(): boolean;
    start(): void;
    done(force?: boolean): void;
  }

  const NProgress: NProgressStatic;

  export default NProgress;
}
