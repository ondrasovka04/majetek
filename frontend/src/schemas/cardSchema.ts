import { z } from "zod";

/**
 * Zod schema for card form validation.
 * Used as the source of truth with React Hook Form via zodResolver.
 *
 * String fields that represent "empty" use empty string "" which the
 * `emptyToNull` transform converts to null before sending to the API.
 */

const emptyToNull = (v: string) => (v === "" ? null : v);
const emptyToNullNum = (v: number | null | undefined) => (v == null || Number.isNaN(v) ? null : v);

/** Check that a number has at most `maxDecimals` decimal places and `maxIntDigits` integer digits. */
const isValidPrecision = (val: number | null, maxIntDigits: number, maxDecimals: number): boolean => {
  if (val == null) return true;
  const multiplier = 10 ** maxDecimals;
  const hasValidDecimals = Math.abs(Math.round(val * multiplier) - val * multiplier) < 1e-6;
  const intDigits = val === 0 ? 1 : Math.floor(Math.log10(Math.abs(val))) + 1;
  return hasValidDecimals && intDigits <= maxIntDigits;
};

export const cardSchema = z
  .object({
    name: z
      .string({ error: "Název nemůže být prázdný." })
      .min(1, "Název nemůže být prázdný.")
      .max(100, "Název může mít maximálně 100 znaků."),

    price: z
      .number()
      .nullable()
      .transform(emptyToNullNum)
      .refine((val) => isValidPrecision(val, 36, 2), {
        message: "Cena musí být číslo s maximálně 2 desetinnými místy a 36 číslicemi celkem.",
      }),

    quantity: z
      .number()
      .nullable()
      .transform(emptyToNullNum)
      .refine((val) => isValidPrecision(val, 36, 2), {
        message: "Počet kusů musí být číslo s maximálně 2 desetinnými místy a 36 číslicemi celkem.",
      }),

    serialNumber: z.string().max(30, "Sériové číslo může mít maximálně 30 znaků.").transform(emptyToNull).nullable(),

    locationId: z.string({ error: "Musí být vybrané umístění." }).min(1, "Musí být vybrané umístění."),

    groupId: z.string({ error: "Musí být vybraná skupina." }).min(1, "Musí být vybraná skupina."),

    classId: z.string().transform(emptyToNull).nullable(),
    purchaseMethodId: z.string().transform(emptyToNull).nullable(),

    purchaseDate: z.date({
      error: "Datum pořízení musí být zadáno.",
    }),

    purchaseInvoice: z.string().max(30, "Faktura může mít maximálně 30 znaků.").transform(emptyToNull).nullable(),

    writeOffDate: z.date().nullable(),

    writeOffMethodId: z.string().transform(emptyToNull).nullable(),

    responsiblePersonId: z.string().transform(emptyToNull).nullable(),
    note: z.string().transform(emptyToNull).nullable(),

    // Read-only fields present in detail view
    id: z.string().optional(),
    createdById: z.string().optional(),
    createdDate: z.date().optional(),
  })
  .refine(
    (data) => {
      const hasDate = !!data.writeOffDate;
      const hasMethod = !!data.writeOffMethodId;
      return hasDate === hasMethod;
    },
    {
      message: "Při vyřazení majetku se musí uvést datum i způsob vyřazení.",
      path: ["writeOffDate"],
    },
  );

/** Form input values — before Zod transforms (nullable strings stay as "") */
export type CardFormValues = z.input<typeof cardSchema>;

/** Validated output values — after Zod transforms (empty strings → null) */
export type CardFormOutput = z.output<typeof cardSchema>;

/**
 * Validate card data and return first error message, or null if valid.
 */
export function validateCard(data: unknown): string | null {
  const result = cardSchema.safeParse(data);
  if (result.success) return null;
  return result.error.issues[0]?.message ?? "Neplatná data.";
}

/**
 * Default values for creating a new card.
 */
export const defaultCardValues: CardFormValues = {
  name: "",
  price: null,
  quantity: 1,
  serialNumber: "",
  locationId: "",
  groupId: "",
  classId: "DHM",
  purchaseMethodId: "",
  purchaseDate: new Date(),
  purchaseInvoice: "",
  writeOffDate: null,
  writeOffMethodId: "",
  responsiblePersonId: "",
  note: "",
};
