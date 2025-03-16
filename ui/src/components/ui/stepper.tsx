
import { cn } from "@/lib/utils";
import { Check } from "lucide-react";

const Dots = () => {
    return <div className="flex flex-col gap-[0.2rem] absolute top-11 left-1/2 -translate-x-1/2">
  
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  <div className="dot h-1 w-1 rounded-full bg-gray-400"></div>
  
  
    </div>
  }

export const Stepper = ({ currentStep }: { currentStep: number }) => {
    const steps = ["Categories", "General", "Variants", "Price & Stock", "Images", "Resume"];
  
    return (
      <nav className="flex flex-col justify-around mb-8 shadow-md px-8 py-4 rounded-md gap-16 h-fit">
        {steps.map((step, index) => (
          <div key={step}>
          <div
            
            className={cn(
              "flex gap-3",
              currentStep > index && "text-muted-foreground",
            )}
          >
            <div
              className={cn(
                "w-10 h-10 rounded-full flex items-center justify-center mr-2 text-[0.6rem] my-auto relative",
                currentStep > index ? "bg-primary text-white shadow-2xl" : "border border-dark",
              )}
            >
              0{index + 1}
              {index < steps.length - 1 ?  <Dots /> : null}
            </div>
            <div className="">
              <p className={"text-[0.6rem] text-gray-500 mb-[0.2rem]"}>STEP 0{index + 1}</p>
              <p className="text-sm mb-[0.3rem]">{step}</p>
              <p className={`text-[0.5rem] flex items-center gap-1 ${currentStep <= index ? "opacity-0" : ""}`}>Completed <Check className="h-2 w-2"/></p>
            </div>  
          </div>
              
          </div>
        ))}
      </nav>
    );
  };
  