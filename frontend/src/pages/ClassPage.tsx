/** Class (třída) dictionary page. */
import DictionaryPage from "@/components/dictionary/DictionaryPage";
import { Class } from "@/types/class";
import { useClasses, useCreateClass, useUpdateClass, useDeleteClass } from "@/hooks/queries/useClassQueries";

const ClassPage = () => (
  <DictionaryPage<Class>
    title="Třída"
    addDialogTitle="Nová třída"
    maxIdLength={10}
    useItems={useClasses}
    useCreate={useCreateClass}
    useUpdate={useUpdateClass}
    useDelete={useDeleteClass}
  />
);

export default ClassPage;
