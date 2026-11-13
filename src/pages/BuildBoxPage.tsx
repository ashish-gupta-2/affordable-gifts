import ProductCatalog from "../components/ProductCatalog";
import OrderSummary from "../components/OrderSummary";
import { useGiftBuilder, MIN_ORDER_VALUE } from "../hooks/useGiftBuilder";

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
    <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-green-50">
        <header className="border-b border-gray-200 bg-white shadow-sm">
            <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
                <div>
                <h1 className="text-3xl font-bold text-green-700">
                    🎁 Affordable Gifts
                </h1>

                <p className="text-sm text-gray-500">
                    Build your personalized dry fruit gift box
                </p>
                </div>

                <div className="rounded-full bg-green-100 px-4 py-2 text-sm font-medium text-green-700">
                Minimum Order ₹{MIN_ORDER_VALUE}
                </div>
            </div>
        </header>

      <main className="mx-auto max-w-7xl p-6">
        <div className="grid gap-10 lg:grid-cols-[2.5fr_1fr]">
          <div>
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
