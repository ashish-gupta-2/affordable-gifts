import type { Packaging } from "../types";
import CatalogImage from "./CatalogImage";

interface Props {
  packagingOptions: Packaging[];
  selectedPackaging: Packaging | null;
  onSelect: (packaging: Packaging) => void;
  selectedCover: Packaging | null;
  onSelectCover: (cover: Packaging) => void;
}

const GROUPS: { kind: Packaging["kind"]; title: string }[] = [
  { kind: "box", title: "🎁 Gift Box" },
  { kind: "jar", title: "🫙 Glass Jars" },
  { kind: "cover", title: "🫙 Glass Jars Cover" },
];

/** Jars are priced per jar, covers per cover, boxes per box. */
const PRICE_SUFFIX: Record<Packaging["kind"], string> = {
  box: "",
  jar: " per jar",
  cover: "",
};

export default function PackagingSelector({
  packagingOptions,
  selectedPackaging,
  onSelect,
  selectedCover,
  onSelectCover,
}: Props) {
  return (
    <section className="mb-10">
      <div className="mb-6">
        <h2 className="text-2xl font-bold">📦 Choose Packaging</h2>
        <p className="text-gray-600">
          Each partition or jar holds one product
        </p>
      </div>

      <div className="space-y-6">
        {GROUPS.map((group) => {
          // Covers are only offered once jars are chosen, and only those that fit them.
          if (group.kind === "cover" && selectedPackaging?.kind !== "jar") {
            return null;
          }
          const options = packagingOptions.filter(
            (option) =>
              option.kind === group.kind &&
              (group.kind !== "cover" ||
                (option.slotCount === selectedPackaging?.slotCount &&
                  option.slotCapacity === selectedPackaging?.slotCapacity))
          );
          const isCover = group.kind === "cover";
          if (options.length === 0 && !isCover) return null;

          return (
            <div key={group.kind}>
              <h3 className="mb-3 font-semibold text-gray-700">
                {group.title}
                {isCover && (
                  <span className="ml-1 text-red-600" aria-hidden="true">*</span>
                )}
              </h3>
              {options.length === 0 && (
                <p className="text-sm text-gray-500">
                  No cover is available for these jars right now.
                </p>
              )}

              <div className="grid gap-4 md:grid-cols-3">
                {options.map((packaging) => {
                  const isSelected = isCover
                    ? selectedCover?.id === packaging.id
                    : selectedPackaging?.id === packaging.id;

                  return (
                    <button
                      key={packaging.id}
                      type="button"
                      aria-pressed={isSelected}
                      onClick={() => (isCover ? onSelectCover(packaging) : onSelect(packaging))}
                      className={`rounded-2xl border-2 bg-white p-5 text-left shadow-sm transition hover:shadow-md ${
                        isSelected
                          ? "border-green-600 bg-green-50"
                          : "border-gray-200"
                      }`}
                    >
                      {packaging.imageUrl && (
                        <div className="mb-3 h-32 overflow-hidden rounded-xl bg-gray-100">
                          <CatalogImage
                            imageUrl={packaging.imageUrl}
                            alt={packaging.name}
                            className="h-full w-full"
                          />
                        </div>
                      )}
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-gray-900">
                          {packaging.name}
                        </span>
                        <span className="font-bold text-green-700">
                          ₹{packaging.price}
                          {PRICE_SUFFIX[packaging.kind] && (
                            <span className="text-xs font-normal text-gray-500">
                              {PRICE_SUFFIX[packaging.kind]}
                            </span>
                          )}
                        </span>
                      </div>

                      <p className="mt-1 text-sm text-gray-500">
                        {packaging.description}
                      </p>
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
