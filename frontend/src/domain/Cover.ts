export type CoverSource = "INVENTAIRE" | "OPEN_LIBRARY" | "BNF" | "LIBRIS";

export interface CoverCandidate {
  source: CoverSource;
  url: string;
}
