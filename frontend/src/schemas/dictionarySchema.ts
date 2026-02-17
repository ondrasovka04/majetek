import { z } from "zod";

/**
 * Creates a Zod schema for dictionary entity forms (id + name).
 * The `maxIdLength` parameter lets pages like ClassPage pass a custom limit.
 */
export function createDictionarySchema(maxIdLength = 20) {
  return z.object({
    id: z
      .string({ error: "ID je povinné." })
      .min(1, "ID je povinné.")
      .max(maxIdLength, `ID nesmí být delší než ${maxIdLength} znaků.`),

    name: z
      .string()
      .max(255, "Název nesmí být delší než 255 znaků.")
      .transform((v) => (v.trim() === "" ? null : v.trim()))
      .nullable(),
  });
}

/** Schema with the default max ID length (20). */
export const dictionarySchema = createDictionarySchema();

/** Schema for validating just the name field (used in inline DictionaryRow edit). */
export const dictionaryNameSchema = z
  .string()
  .max(255, "Název nesmí být delší než 255 znaků.");

/** Form input values — before transform (name is a string). */
export type DictionaryFormValues = z.input<ReturnType<typeof createDictionarySchema>>;

/** Validated output — after transform (empty name → null). */
export type DictionaryFormOutput = z.output<ReturnType<typeof createDictionarySchema>>;
