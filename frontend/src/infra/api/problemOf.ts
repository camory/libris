interface ProblemResponse {
  type: string;
}

export async function problemOf(
  response: Response,
  statuses: Map<number, string>,
): Promise<{ outcome: "problem"; type: string }> {
  const known = statuses.get(response.status);
  const isProblem = response.headers
    .get("Content-Type")
    ?.startsWith("application/problem+json");
  if (known !== undefined && isProblem) {
    return { outcome: "problem", type: known };
  }
  const problem = (await response.json()) as ProblemResponse;
  return { outcome: "problem", type: problem.type };
}
