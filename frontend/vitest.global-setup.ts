import { spawn } from "node:child_process";
import type { TestProject } from "vitest/node";

declare module "vitest" {
  interface ProvidedContext {
    mockBaseUrl: string;
  }
}

const port = 9099;

export default async function setup(project: TestProject) {
  const mock = spawn(
    "contracteer",
    ["mock", "../api/openapi.yaml", "-p", String(port)],
    { stdio: ["ignore", "pipe", "inherit"] },
  );

  await new Promise<void>((resolve, reject) => {
    mock.stdout.on("data", (chunk: Buffer) => {
      if (chunk.toString().includes(`started on port ${port}`)) {
        resolve();
      }
    });
    mock.on("exit", (code) => {
      reject(new Error(`contracteer mock exited with ${code}`));
    });
  });

  project.provide("mockBaseUrl", `http://localhost:${port}`);

  return () => {
    mock.kill();
  };
}
