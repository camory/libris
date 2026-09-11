import { within } from "@testing-library/dom";
import { afterEach, describe, expect, inject, it } from "vitest";
import { bootstrap } from "../bootstrap";

describe("Fast entry", () => {
  const host = document.createElement("div");
  let app: ReturnType<typeof bootstrap>;

  afterEach(() => {
    app.unmount();
  });

  it("the application runs over the mock", async () => {
    // When
    app = bootstrap(inject("mockBaseUrl"), "sha-abc1234");
    app.mount(host);

    // Then
    const screen = within(host);
    expect(await screen.findByText(/^Bonjour /)).toBeDefined();
    expect(screen.getByText("sha-abc1234")).toBeDefined();
  });
});
