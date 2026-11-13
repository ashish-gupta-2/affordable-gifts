import type { Product } from "../types";

interface Props {
  product: Product;
}

export default function ProductCard({ product }: Props) {
  return (
    <div className="overflow-hidden rounded-xl border bg-white shadow-sm transition hover:shadow-md">
      <div className="flex h-40 items-center justify-center bg-gray-100">
        <span className="text-gray-400">
          Product Image
        </span>
      </div>

      <div className="p-4">
        <h3 className="font-semibold text-gray-900">
          {product.name}
        </h3>

        <p className="mt-1 text-sm text-gray-500">
          {product.category}
        </p>

        <div className="mt-2 flex items-center justify-between">
          <span className="font-bold text-green-700">
            ₹{product.price}
          </span>

          <span className="text-sm text-gray-500">
            {product.weight}g
          </span>
        </div>

        <button
          className="mt-4 w-full rounded-lg bg-green-600 py-2 text-white hover:bg-green-700"
        >
          Add to Box
        </button>
      </div>
    </div>
  );
}