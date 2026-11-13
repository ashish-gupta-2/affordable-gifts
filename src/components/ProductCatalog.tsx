import type { Product } from "../types";
import { products } from "../data/products";
import ProductCard from "./ProductCard";

interface Props {
  onAdd: (product: Product) => void;
}

export default function ProductCatalog({
  onAdd,
}: Props) {
  return (
    <section>
      <div className="mb-6">
        <h2 className="text-2xl font-bold">
          Select Products
        </h2>

        <p className="text-gray-600">
          Choose dry fruits and snacks
        </p>
      </div>

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