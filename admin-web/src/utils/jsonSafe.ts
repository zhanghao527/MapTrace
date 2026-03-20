/**
 * Parse JSON safely: convert 16+ digit bare integers to strings before JSON.parse.
 * This prevents IEEE754 precision loss for snowflake-like IDs.
 */
export function parseJsonPreservingBigInts(raw: unknown) {
  if (raw == null || raw === '') return raw;
  if (typeof raw !== 'string') return raw;

  const trimmed = raw.trim();
  if (!trimmed) return raw;

  let fixed = trimmed.replace(
    /"([^"\\]+)"\s*:\s*(\d{16,})(?=\s*[,}\]])/g,
    '"$1":"$2"'
  );
  fixed = fixed.replace(
    /(\[|,)\s*(\d{16,})(?=\s*[,}\]])/g,
    '$1"$2"'
  );
  return JSON.parse(fixed);
}
