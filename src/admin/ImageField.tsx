import { useState } from "react";
import CatalogImage from "../components/CatalogImage";
import type { AdminApi } from "./api";
import { secondaryButton } from "./ui";

interface Props {
  api: AdminApi;
  value: string | null;
  onChange: (url: string | null) => void;
}

const MAX_BYTES = 1_000_000;

/** Pick a photo, upload it at once, and show a preview. */
export default function ImageField({ api, value, onChange }: Props) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const pick = async (file: File | undefined) => {
    if (!file) return;
    setError(null);
    if (file.size > MAX_BYTES) {
      setError("Image must be 1 MB or smaller.");
      return;
    }
    setBusy(true);
    try {
      onChange(await api.uploadImage(file));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Upload failed");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="md:col-span-3">
      <span className="mb-1 block text-sm font-medium">Photo</span>
      <div className="flex items-center gap-4">
        <div className="h-20 w-20 overflow-hidden rounded-xl bg-gray-100">
          {value ? (
            <CatalogImage imageUrl={value} alt="Preview" className="h-full w-full" />
          ) : (
            <div className="flex h-full items-center justify-center text-xs text-gray-400">
              No photo
            </div>
          )}
        </div>
        <div className="space-y-2">
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp"
            disabled={busy}
            onChange={(e) => {
              void pick(e.target.files?.[0]);
              e.target.value = "";
            }}
            className="block text-sm"
          />
          {value && (
            <button type="button" className={secondaryButton} onClick={() => onChange(null)}>
              Remove photo
            </button>
          )}
          <p className="text-xs text-gray-500">
            {busy ? "Uploading…" : "JPG, PNG or WebP, up to 1 MB. Square photos look best."}
          </p>
        </div>
      </div>
      {error && <p role="alert" className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  );
}
