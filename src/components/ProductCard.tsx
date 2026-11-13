import type { Product } from "../types";

interface Props {
  product: Product;
  onAdd: (product: Product) => void;
}

export default function ProductCard({
  product,
  onAdd,
}: Props) {
  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg">
      <div className="flex h-40 items-center justify-center bg-gradient-to-br from-yellow-50 to-orange-100">
        <span className="text-5xl">
            {product.emoji}
        </span>
      </div>

      <div className="p-5">
        <h3 className="text-lg font-semibold text-gray-900">
          {product.name}
        </h3>

        <span className="mt-3 inline-block rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700">
            {product.category}
        </span>

        <div className="mt-4 flex items-center justify-between">
            <span className="text-xl font-bold text-green-700">
                ₹{product.price}
            </span>

            <span className="rounded-full bg-gray-100 px-3 py-1 text-sm">
                {product.weight}g
            </span>
        </div>

        <button
          onClick={() => onAdd(product)}
          className="mt-5 w-full rounded-xl bg-green-600 py-3 font-medium text-white transition hover:bg-green-700">
          Add To Box
        </button>
      </div>
    </div>
  );
}