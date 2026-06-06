/**
 * 解析后端时间字符串。
 *
 * 规则：
 * - 如果带时区（Z 或 ±hh:mm），按原值解析。
 * - 如果不带时区，默认按 UTC 解析，避免不同时区显示偏差。
 * - 兼容 "YYYY-MM-DD HH:mm:ss" 与 "YYYY-MM-DD" 格式。
 */
export function parseServerDate(value?: string | null): Date | null {
  if (!value) return null;
  const trimmed = value.trim();
  if (!trimmed) return null;

  let normalized = trimmed;

  if (/^\d{4}-\d{2}-\d{2}$/.test(normalized)) {
    normalized = `${normalized}T00:00:00`;
  } else if (!normalized.includes("T") && normalized.includes(" ")) {
    normalized = normalized.replace(" ", "T");
  }

  const hasTimezone = /([zZ]|[+-]\d{2}:?\d{2})$/.test(normalized);
  if (!hasTimezone) {
    normalized = `${normalized}Z`;
  }

  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return null;
  return date;
}
