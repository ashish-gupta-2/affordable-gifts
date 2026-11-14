import ProductCatalog from "../components/ProductCatalog";
import OrderSummary from "../components/OrderSummary";
import { useGiftBuilder } from "../hooks/useGiftBuilder";

export default function BuildBoxPage() {
  const {
    items,
    addProduct,
    removeProduct,
    totalPrice,
    totalWeight,
    remainingAmount,
  } = useGiftBuilder();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-green-700 text-white py-6 shadow">
        <div className="mx-auto max-w-7xl px-4">
          <h1 className="text-3xl font-bold">
            Affordable Gifts
          </h1>

          <p className="mt-1">
            Build Your Own Gift Box
          </p>
        </div>
      </header>

      <main className="mx-auto max-w-7xl p-6">
        <div className="grid gap-8 lg:grid-cols-3">
          <div className="lg:col-span-2">
            <ProductCatalog
              onAdd={addProduct}
            />
          </div>

          <OrderSummary
            items={items}
            totalPrice={totalPrice}
            totalWeight={totalWeight}
            remainingAmount={remainingAmount}
            onAdd={addProduct}
  onRemove={removeProduct}
          />
        </div>
      </main>
    </div>
  );
}
