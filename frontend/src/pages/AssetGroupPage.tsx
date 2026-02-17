/** Asset group (skupina majetku) dictionary page. */
import DictionaryPage from "../components/dictionary/DictionaryPage";
import { AssetGroup } from "../types/assetGroup";
import {
  useAssetGroups,
  useCreateAssetGroup,
  useUpdateAssetGroup,
  useDeleteAssetGroup,
} from "../hooks/queries/useAssetGroupQueries";

const AssetGroupPage = () => (
  <DictionaryPage<AssetGroup>
    title="Skupiny"
    addDialogTitle="Nová skupina"
    useItems={useAssetGroups}
    useCreate={useCreateAssetGroup}
    useUpdate={useUpdateAssetGroup}
    useDelete={useDeleteAssetGroup}
  />
);

export default AssetGroupPage;
