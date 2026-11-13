import { products } from "../data/products";
import type { Product } from "../types";
import ProductCard from "./ProductCard";

interface Props {
  onAdd: (product: Product) => void;
}

export default function ProductCatalog({
  onAdd,
}: Props) {
  return (
    <section>
      <h2 className="mb-6 text-2xl font-bold">
        Select Products
      </h2>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            onAdd={onAdd}
          />
        ))}
      </div>
    </section>
  );
}