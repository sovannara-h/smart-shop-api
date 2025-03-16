import { FormRowInput } from "@/components/form/form-row-input";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Form } from "@/components/ui/form";
import { CirclePlus } from "lucide-react";
import { useCategoryForm } from "../hooks/use-category-form";

export type  CategoryFormProps = {

}

export const CategoryForm = (props: CategoryFormProps) => {

    const {} = props;

    const {form , onSubmit, open, setOpen} = useCategoryForm();

    return (
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
        <Button variant="outline">
          <CirclePlus />
          Create category
          </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>New Category</DialogTitle>
          <DialogDescription>
            Create a new category to organize your products.
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 max-w-xl" onClick={e => e.stopPropagation()}>
          <FormRowInput 
            label="Name" 
            name="name" 
            required 
          />

          <div className="flex justify-end gap-4">

            <Button type="submit" >
              Create category
            </Button>
          </div>
        </form>
      </Form>

        {/* <DialogFooter>
          <Button type="submit">Save changes</Button>
        </DialogFooter> */}
      </DialogContent>


      
      </Dialog>
    );
}
