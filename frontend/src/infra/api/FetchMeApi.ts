import type { MeApi } from "../../application/MeApi";
import type { Reader } from "../../domain/Reader";

interface CurrentReaderResponse {
  id: string;
  username: string;
  displayName: string;
  email: string;
  role: "READER" | "ADMIN";
}

export class FetchMeApi implements MeApi {
  constructor(private readonly baseUrl: string) {}

  async currentReader(): Promise<Reader> {
    const response = await fetch(`${this.baseUrl}/api/v1/me`, {
      headers: { Accept: "application/json" },
    });
    const body = (await response.json()) as CurrentReaderResponse;
    return {
      id: body.id,
      username: body.username,
      displayName: body.displayName,
      email: body.email,
      role: body.role,
    };
  }
}
