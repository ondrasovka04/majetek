/** Shared base interface for all dictionary entities (AssetGroup, Class, Location, etc.) */
export interface DictionaryEntity {
  id: string;
  name: string | null;
}
