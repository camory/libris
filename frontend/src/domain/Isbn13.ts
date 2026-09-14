export function isbn13Of(text: string): string | null {
  const characters = text.replaceAll("-", "").replaceAll(" ", "");
  const digits =
    characters.length === 10 ? thirteenOf(characters) : characters;
  return checkDigitOf(digits.slice(0, 12)) === digits.slice(12) ? digits : null;
}

function thirteenOf(ten: string): string {
  const twelve = `978${ten.slice(0, 9)}`;
  return twelve + checkDigitOf(twelve);
}

function checkDigitOf(twelveDigits: string): string {
  const weighted = [...twelveDigits].reduce(
    (sum, digit, index) => sum + Number(digit) * (index % 2 === 0 ? 1 : 3),
    0,
  );
  return String((10 - (weighted % 10)) % 10);
}
