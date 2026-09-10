export {};

declare module "vitest" {
  interface ProvidedContext {
    mockBaseUrl: string;
  }
}
