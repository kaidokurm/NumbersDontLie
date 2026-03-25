export function Alert({
  title,
  message,
  tone = "warning",
  children,
}: {
  title?: string;
  message?: string;
  tone?: "warning" | "error" | "info" | "success";
  children?: React.ReactNode;
}) {
  const styles =
    tone === "error"
      ? "border-red-300 bg-red-50 text-red-900"
      : tone === "success"
        ? "border-green-300 bg-green-50 text-green-900"
        : tone === "info"
          ? "border-blue-300 bg-blue-50 text-blue-900"
          : "border-amber-300 bg-amber-50 text-amber-900";

  return (
    <div className={`rounded-lg border p-3 text-sm ${styles}`}>
      {title && <div className="font-semibold">{title}</div>}
      {message && <div className="wrap-break-word">{message}</div>}
      {children && <div className="wrap-break-word">{children}</div>}
    </div>
  );
}
