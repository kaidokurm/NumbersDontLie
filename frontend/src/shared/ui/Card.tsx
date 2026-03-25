import type { ReactNode } from "react";

export function Card({ children }: { children: ReactNode }) {
  return <div className="rounded-xl border bg-white p-4 md:p-6">{children}</div>;
}

export function CardTitle({ children }: { children: ReactNode }) {
  return <div className="text-base font-semibold">{children}</div>;
}

export function CardSubtitle({ children }: { children: ReactNode }) {
  return <div className="mt-1 text-sm text-gray-600">{children}</div>;
}

export function CardBody({ children }: { children: ReactNode }) {
  return <div className="mt-3">{children}</div>;
}
