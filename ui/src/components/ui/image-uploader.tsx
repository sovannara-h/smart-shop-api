// import { cn } from '@/lib/utils';

import * as LR from "@uploadcare/blocks";
import { FC, useEffect, useRef } from "react";
import { cn } from "../../lib/utils";

export const ImageUploader: FC<{
  className?: string;
  ctxName: string;
  cb: (images: any[]) => void;
  multiple?: boolean;
}> = ({ className = "", cb, ctxName, multiple = false }) => {
  LR.registerBlocks(LR);

  const ctxProviderRef = useRef<
    typeof LR.UploadCtxProvider.prototype & LR.UploadCtxProvider
  >(null);

  useEffect(() => {
    const handleUpload = (e: CustomEvent<LR.OutputFileEntry[]>) => {
      if (e.detail && e.detail.length > 0) {
        const images = e.detail;
        cb(images);
      }
    };

    //@ts-ignore
    ctxProviderRef.current?.addEventListener("data-output", handleUpload);
  }, []);

  return (
    <div className={cn("", className)}>
      <div>
        <lr-config
          ctx-name={ctxName}
          pubkey="09b12df0d53f8c018249"
          multiple={multiple}
        />
        <lr-file-uploader-regular
          ctx-name={ctxName}
          css-src={`https://cdn.jsdelivr.net/npm/@uploadcare/blocks@0.30.8/web/lr-file-uploader-regular.min.css`}
        />
        <lr-upload-ctx-provider
          ref={ctxProviderRef}
          ctx-name={ctxName}
          data-cy={"btn_img_upload"}
        />
      </div>
    </div>
  );
};

// import { FileUploaderRegular } from "@uploadcare/react-uploader";
// import "@uploadcare/react-uploader/core.css";


// export type  ImageUploaderProps = {

// }

// export const ImageUploader = (props: ImageUploaderProps) => {

//     const {} = props;

//     return (
//       <FileUploaderRegular pubkey="YOUR_PUBLIC_KEY"/>;
//     );
// }