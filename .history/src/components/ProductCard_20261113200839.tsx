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
    <div className="overflow-hidden rounded-xl border bg-white shadow-sm">
      <div className="flex h-40 items-center justify-center bg-gray-100">
        Product Image
      </div>

      <div className="p-4">
        <h3 className="font-semibold">
          {product.name}
        </h3>

        <p className="text-sm text-gray-500">
          {product.category}
        </p>

        <div className="mt-2 flex justify-between">
          <span>₹{product.price}</span>
          <span>{product.weight}g</span>
        </div>

        <button
          onClick={() => onAdd(product)}
          className="mt-4 w-full rounded-lg bg-green-600 py-2 text-white"
        >
          Add to Box
        </button>
      </div>
    </div>
  );
}