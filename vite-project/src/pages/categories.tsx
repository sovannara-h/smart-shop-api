import { DataTable } from "@/components/ui/data-table";
import { useFetchDataWithPageable } from "@/hooks/useFetchDataWithPageable";
import { ColumnDef } from "@tanstack/react-table";

const columns: ColumnDef<any>[] = [
    {
      accessorKey: "id",
      header: "id",
    },
    {
      accessorKey: "name",
      header: "name",
    },
]


export type  CategoriesProps = {

}

export const Categories = (props: CategoriesProps) => {

    const {} = props;

    const {handlePageChange, pageInfo, queryRes: {data, isLoading}} = useFetchDataWithPageable({table: "categories"});

    const categories = data?.data.content || []
    return (
      <div className={`Categories`}>
        <DataTable columns={columns} data={categories} onPageChange={handlePageChange} pageInfo={pageInfo}/>
      </div>
    );
}