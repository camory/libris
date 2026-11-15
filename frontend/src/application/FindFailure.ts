export type FindFailure = { status: "notFound" } | { status: "unavailable" };

export function findFailureOf(type: string): FindFailure {
  switch (type) {
    case "/problems/not-found":
    case "/problems/validation":
      return { status: "notFound" };
    default:
      return { status: "unavailable" };
  }
}
