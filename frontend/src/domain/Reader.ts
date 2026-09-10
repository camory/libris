export type Role = "READER" | "ADMIN";

export interface Reader {
  id: string;
  username: string;
  displayName: string;
  email: string;
  role: Role;
}
