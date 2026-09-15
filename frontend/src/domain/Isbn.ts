const thirteenDigits = /^97[89][0-9]{10}$/;
const tenCharacters = /^[0-9]{9}[0-9Xx]$/;

export class Isbn {
  private constructor(readonly digits: string) {}

  static of(text: string): Isbn | null {
    const characters = text.replaceAll("-", "").replaceAll(" ", "");
    const digits =
      characters.length === 10 ? thirteenOf(characters) : characters;
    if (digits === null || !thirteenDigits.test(digits)) {
      return null;
    }
    return checkDigitOf(digits.slice(0, 12)) === digits.slice(12)
      ? new Isbn(digits)
      : null;
  }
}

function thirteenOf(ten: string): string | null {
  if (!tenCharacters.test(ten)) {
    return null;
  }
  const nine = ten.slice(0, 9);
  const weighted =
    [...nine].reduce(
      (sum, digit, index) => sum + Number(digit) * (10 - index),
      0,
    ) + checkValueOf(ten.slice(9));
  if (weighted % 11 !== 0) {
    return null;
  }
  const twelve = `978${nine}`;
  return twelve + checkDigitOf(twelve);
}

function checkValueOf(character: string): number {
  return character.toUpperCase() === "X" ? 10 : Number(character);
}

function checkDigitOf(twelveDigits: string): string {
  const weighted = [...twelveDigits].reduce(
    (sum, digit, index) => sum + Number(digit) * (index % 2 === 0 ? 1 : 3),
    0,
  );
  return String((10 - (weighted % 10)) % 10);
}
