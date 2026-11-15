import type { Product } from "../types";

/** Tiny inline picture for lists: the photo if there is one, otherwise the emoji. */
export default function ProductThumb({ product }: { product: Product }) {
  if (!product.imageUrl) return <>{product.emoji}</>;
  return (
    <img
      src={product.imageUrl}
      alt=""
      className="mr-1 inline-block h-6 w-6 rounded object-cover align-middle"
    />
  );
}
