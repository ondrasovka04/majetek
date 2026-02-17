/** Purchase method (způsob pořízení) dictionary page. */
import DictionaryPage from "../components/dictionary/DictionaryPage";
import { PurchaseMethod } from "../types/purchaseMethod";
import {
  usePurchaseMethods,
  useCreatePurchaseMethod,
  useUpdatePurchaseMethod,
  useDeletePurchaseMethod,
} from "../hooks/queries/usePurchaseMethodQueries";

const PurchaseMethodPage = () => (
  <DictionaryPage<PurchaseMethod>
    title="Způsob pořízení"
    addDialogTitle="Nový způsob pořízení"
    useItems={usePurchaseMethods}
    useCreate={useCreatePurchaseMethod}
    useUpdate={useUpdatePurchaseMethod}
    useDelete={useDeletePurchaseMethod}
  />
);

export default PurchaseMethodPage;
