/** Shared form fields for card create/edit views — text inputs, selects, date pickers, and price inputs. */
import { TextField, Grid, MenuItem, Box } from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers";
import { Controller, useFormContext } from "react-hook-form";
import { PriceInput } from "@/components/card/PriceInput";
import { CardFormValues } from "@/schemas/cardSchema";
import { Location } from "@/types/location";
import { AssetGroup } from "@/types/assetGroup";
import { Class } from "@/types/class";
import { PurchaseMethod } from "@/types/purchaseMethod";
import { WriteOffMethod } from "@/types/writeOffMethod";
import { User } from "@/types/auth";

interface Props {
  locations: Location[];
  groups: AssetGroup[];
  classes: Class[];
  purchaseMethods: PurchaseMethod[];
  writeOffMethods: WriteOffMethod[];
  users: User[];
  /** Show read-only createdDate/createdById fields (detail view only) */
  showReadOnlyFields?: boolean;
}

/** Renders a select item with ID + name columns */
const DictionaryMenuItem = ({ id, name }: { id: string; name: string | null }) => (
  <MenuItem key={id} value={id}>
    <Box
      sx={{
        display: "flex",
        gap: 2,
        width: "100%",
      }}>
      <span style={{ minWidth: 60 }}>{id}</span>
      <span>{name}</span>
    </Box>
  </MenuItem>
);

/**
 * Shared card form fields powered by React Hook Form context.
 * Must be used inside a FormProvider / useFormContext wrapper.
 */
const CardFormFields = ({
  locations,
  groups,
  classes,
  purchaseMethods,
  writeOffMethods,
  users,
  showReadOnlyFields = false,
}: Props) => {
  const {
    register,
    control,
    formState: { errors },
    watch,
  } = useFormContext<CardFormValues>();

  return (
    <Grid container spacing={2}>
      {/* --- Basic info --- */}
      <Grid size={{ xs: 6 }}>
        <TextField
          label="Název"
          required
          fullWidth
          {...register("name")}
          error={!!errors.name}
          helperText={errors.name?.message}
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
        <Controller
          name="price"
          control={control}
          render={({ field }) => (
            <PriceInput label="Cena" value={field.value ?? null} onChange={(val) => field.onChange(val)} />
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <Controller
          name="quantity"
          control={control}
          render={({ field }) => (
            <TextField
              label="Počet kusů"
              type="number"
              fullWidth
              value={field.value ?? ""}
              onChange={(e) => {
                const v = Number.parseFloat(e.target.value);
                field.onChange(Number.isNaN(v) ? null : v);
              }}
              error={!!errors.quantity}
              helperText={errors.quantity?.message}
            />
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <TextField
          label="Sériové číslo"
          fullWidth
          {...register("serialNumber")}
          error={!!errors.serialNumber}
          helperText={errors.serialNumber?.message}
        />
      </Grid>

      <Grid size={{ xs: 12 }} sx={{ my: 2 }} />

      {/* --- References --- */}
      <Grid size={{ xs: 6 }}>
        <Controller
          name="locationId"
          control={control}
          render={({ field }) => (
            <TextField
              label="Umístění"
              select
              required
              fullWidth
              value={field.value ?? ""}
              onChange={field.onChange}
              error={!!errors.locationId}
              helperText={errors.locationId?.message}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const loc = locations.find((l) => l.id === value);
                    return loc ? `${value} - ${loc.name ?? ""}` : String(value);
                  },
                },
              }}>
              {locations.map((l) => (
                <DictionaryMenuItem key={l.id} id={l.id} name={l.name} />
              ))}
            </TextField>
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <Controller
          name="classId"
          control={control}
          render={({ field }) => (
            <TextField
              label="Třída"
              select
              fullWidth
              value={field.value ?? ""}
              onChange={field.onChange}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => (value as string) || "(nevybráno)",
                },
              }}>
              <MenuItem value="">(nevybráno)</MenuItem>
              {classes.map((c) => (
                <DictionaryMenuItem key={c.id} id={c.id} name={c.name} />
              ))}
            </TextField>
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <Controller
          name="groupId"
          control={control}
          render={({ field }) => (
            <TextField
              label="Skupina"
              select
              required
              fullWidth
              value={field.value ?? ""}
              onChange={field.onChange}
              error={!!errors.groupId}
              helperText={errors.groupId?.message}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => {
                    const group = groups.find((g) => g.id === value);
                    return group ? `${value} - ${group.name ?? ""}` : String(value);
                  },
                },
              }}>
              {groups.map((g) => (
                <DictionaryMenuItem key={g.id} id={g.id} name={g.name} />
              ))}
            </TextField>
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <Controller
          name="purchaseMethodId"
          control={control}
          render={({ field }) => (
            <TextField
              label="Způsob pořízení"
              select
              fullWidth
              value={field.value ?? ""}
              onChange={field.onChange}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => (value as string) || "(nevybráno)",
                },
              }}>
              <MenuItem value="">(nevybráno)</MenuItem>
              {purchaseMethods.map((m) => (
                <DictionaryMenuItem key={m.id} id={m.id} name={m.name} />
              ))}
            </TextField>
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <TextField
          label="Faktura"
          fullWidth
          {...register("purchaseInvoice")}
          error={!!errors.purchaseInvoice}
          helperText={errors.purchaseInvoice?.message}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <Controller
          name="purchaseDate"
          control={control}
          render={({ field }) => (
            <DatePicker
              label="Datum pořízení *"
              value={field.value ?? null}
              onChange={(date) => field.onChange(date)}
              slotProps={{
                textField: {
                  fullWidth: true,
                  error: !!errors.purchaseDate,
                  helperText: errors.purchaseDate?.message,
                },
              }}
            />
          )}
        />
      </Grid>

      <Grid size={{ xs: 12 }} sx={{ my: 2 }} />

      {/* --- Write-off --- */}
      <Grid size={{ xs: 6 }}>
        <Controller
          name="writeOffDate"
          control={control}
          render={({ field }) => (
            <DatePicker
              label="Datum vyřazení"
              value={field.value ?? null}
              onChange={(date) => field.onChange(date)}
              slotProps={{
                textField: {
                  fullWidth: true,
                  error: !!errors.writeOffDate,
                  helperText: errors.writeOffDate?.message,
                },
              }}
            />
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <Controller
          name="writeOffMethodId"
          control={control}
          render={({ field }) => (
            <TextField
              label="Způsob vyřazení"
              select
              fullWidth
              value={field.value ?? ""}
              onChange={field.onChange}
              slotProps={{
                select: {
                  renderValue: (value: unknown) => (value as string) || "(nevybráno)",
                },
              }}>
              <MenuItem value="">(nevybráno)</MenuItem>
              {writeOffMethods.map((m) => (
                <DictionaryMenuItem key={m.id} id={m.id} name={m.name} />
              ))}
            </TextField>
          )}
        />
      </Grid>

      <Grid size={{ xs: 6 }}>
        <Controller
          name="responsiblePersonId"
          control={control}
          render={({ field }) => (
            <TextField label="Zodpovědná osoba" select fullWidth value={field.value ?? ""} onChange={field.onChange}>
              <MenuItem value="">(nevybráno)</MenuItem>
              {users.map((u) => (
                <MenuItem key={u.id} value={u.id}>
                  {u.name}
                </MenuItem>
              ))}
            </TextField>
          )}
        />
      </Grid>

      <Grid size={{ xs: 12 }}>
        <TextField label="Poznámka" multiline fullWidth minRows={2} {...register("note")} />
      </Grid>

      {/* --- Read-only metadata (detail view) --- */}
      {showReadOnlyFields && (
        <>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Vytvořeno"
              fullWidth
              value={(() => {
                const created = watch("createdDate");
                return created ? new Date(created).toLocaleDateString("cs-CZ") : "";
              })()}
              disabled
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField label="Vytvořil" fullWidth value={watch("createdById") ?? ""} disabled />
          </Grid>
        </>
      )}
    </Grid>
  );
};

export default CardFormFields;
