import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import AppFooter from "./AppFooter.vue";

describe("AppFooter", () => {
  it("shows the revision it is given", () => {
    const wrapper = mount(AppFooter, { props: { revision: "sha-abc1234" } });

    expect(wrapper.get("footer").text()).toContain("sha-abc1234");
  });
});
