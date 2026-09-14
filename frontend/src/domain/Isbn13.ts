export function isbn13Of(text: string): string | null {
  const digits = text.replaceAll("-", "").replaceAll(" ", "");
  return checkDigitOf(digits.slice(0, 12)) === digits.slice(12) ? digits : null;
}

function checkDigitOf(twelveDigits: string): string {
  const weighted = [...twelveDigits].reduce(
    (sum, digit, index) => sum + Number(digit) * (index % 2 === 0 ? 1 : 3),
    0,
  );
  return String((10 - (weighted % 10)) % 10);
}
