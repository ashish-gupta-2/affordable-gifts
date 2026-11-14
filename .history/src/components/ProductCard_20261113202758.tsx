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
    <div className="overflow-hidden rounded-xl border bg-white shadow-sm hover:shadow-md">
      <div className="mt-3 flex items-center justify-between">
        <span className="text-gray-400">
          Product Image
        </span>
      </div>

      <div className="p-4">
        <h3 className="font-semibold">
          {product.name}
        </h3>

        <p className="text-sm text-gray-500">
          {product.category}
        </p>

        <div className="mt-3 flex justify-between">
          <span className="font-bold text-green-700">
            ₹{product.price}
        </span>

          <span>{product.weight}g</span>
        </div>

        <button
          onClick={() => onAdd(product)}
          className="mt-4 w-full rounded-lg bg-green-600 py-2 text-white hover:bg-green-700"
        >
          Add To Box
        </button>
      </div>
    </div>
  );
}