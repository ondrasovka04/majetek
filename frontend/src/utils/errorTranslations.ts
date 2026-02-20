/** Translates English backend error messages to Czech UI strings using regex patterns and a static lookup map. */
export function translateError(message: string): string {
  if (!message) return "Neznámá chyba.";

  let match = /^Location with id (.+) already exists$/.exec(message);
  if (match) {
    const id = match[1];
    return `Umístění s ID ${id} již existuje.`;
  }

  match = /^Asset group with id (.+) already exists$/.exec(message);
  if (match) {
    const id = match[1];
    return `Skupina s ID ${id} již existuje.`;
  }

  match = /^Class with id (.+) already exists$/.exec(message);
  if (match) {
    const id = match[1];
    return `Třída s ID ${id} již existuje.`;
  }

  match = /^Purchase method with id (.+) already exists$/.exec(message);
  if (match) {
    const id = match[1];
    return `Způsob pořízení s ID ${id} již existuje.`;
  }

  match = /^Write off method with id (.+) already exists$/.exec(message);
  if (match) {
    const id = match[1];
    return `Způsob vyřazení s ID ${id} již existuje.`;
  }

  match = /^Printing more than (.+) cards is forbidden$/.exec(message);
  if (match) {
    const count = match[1];
    return `Tisk více než ${count} štítků není povolen.`;
  }

  const staticTranslations: Record<string, string> = {
    "Invalid username or password": "Špatné přihlašovací údaje",
    "Location cannot be deleted because it is being used.": "Umístění nelze smazat, protože je používáno.",
    "User Id is already taken": "Uživatelské ID je již obsazeno.",
    "Username is already taken": "Uživatelské jméno je již obsazeno.",
    "Asset group cannot be deleted because it is being used.": "Skupinu nelze smazat, protože je používána.",
    "Class cannot be deleted because it is being used.": "Třídu nelze smazat, protože je používána.",
    "Purchase method cannot be deleted because it is being used.": "Způsob pořízení nelze smazat, protože je používán.",
    "Write off method cannot be deleted because it is being used.":
      "Způsob vyřazení nelze smazat, protože je používán.",
    "Validation failed": "Chyba validace vstupu",
    "Maximum upload size exceeded": "Příloha je příliš velká",
  };

  return staticTranslations[message] || message;
}
