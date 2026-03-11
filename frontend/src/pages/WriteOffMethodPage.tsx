/** Write-off method (způsob vyřazení) dictionary page. */
import DictionaryPage from "@/components/dictionary/DictionaryPage";
import { WriteOffMethod } from "@/types/writeOffMethod";
import {
  useWriteOffMethods,
  useCreateWriteOffMethod,
  useUpdateWriteOffMethod,
  useDeleteWriteOffMethod,
} from "@/hooks/queries/useWriteOffMethodQueries";

const WriteOffMethodPage = () => (
  <DictionaryPage<WriteOffMethod>
    title="Způsob vyřazení"
    addDialogTitle="Nový způsob vyřazení"
    useItems={useWriteOffMethods}
    useCreate={useCreateWriteOffMethod}
    useUpdate={useUpdateWriteOffMethod}
    useDelete={useDeleteWriteOffMethod}
  />
);

export default WriteOffMethodPage;
