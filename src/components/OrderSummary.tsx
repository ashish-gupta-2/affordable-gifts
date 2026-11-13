import type { CartItem } from "../types";
import { MIN_ORDER_VALUE } from "../hooks/useGiftBuilder";

interface Props {
  items: CartItem[];
  totalPrice: number;
  totalWeight: number;
  remainingAmount: number;
  onAdd: (product: CartItem["product"]) => void;
  onRemove: (productId: string) => void;
}

export default function OrderSummary({
  items,
  totalPrice,
  totalWeight,
  remainingAmount,
  onAdd,
  onRemove,
}: Props) {
  return (
  <div className="sticky top-6 self-start rounded-3xl bg-white p-6 shadow-xl">
    <h2 className="mb-6 text-2xl font-bold text-gray-900">
        🛒 Your Box
    </h2>

    {items.length === 0 ? (
      <div className="rounded-lg bg-gray-50 p-4 text-gray-500">
        <div className="py-10 text-center">
            <div className="text-5xl">🎁</div>
                <p className="mt-4 text-gray-500">
                    Your gift box is empty
                </p>
            </div>
      </div>
    ) : (
      <div className="space-y-4">
        {items.map((item) => (
          <div
            key={item.product.id}
            className="rounded-xl border border-gray-100 bg-gray-50 p-4"
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="font-semibold">
                  {item.product.name}
                </p>

                <p className="text-sm text-gray-500">
                  ₹{item.product.price} each
                </p>
              </div>

              <div className="font-bold text-green-700">
                ₹
                {item.product.price *
                  item.quantity}
              </div>
            </div>

            <div className="mt-3 flex items-center gap-3">
              <button
                onClick={() =>
                  onRemove(item.product.id)
                }
                className="flex h-9 w-9 items-center justify-center rounded-full bg-red-500 font-bold text-white transition hover:bg-red-600"
              >
                -
              </button>

              <span className="min-w-[30px] text-center text-lg font-semibold">
                {item.quantity}
              </span>

              <button
                onClick={() =>
                  onAdd(item.product)
                }
                className="flex h-9 w-9 items-center justify-center rounded-full bg-green-600 font-bold text-white transition hover:bg-green-700"
              >
                +
              </button>
            </div>
          </div>
        ))}
      </div>
    )}

    <hr className="my-6 border-gray-200" />

    <div className="mt-6 rounded-xl bg-green-50 p-4">
        <div className="mb-2 flex justify-between">
            <span>Total Weight</span>
            <span className="font-semibold">
            {totalWeight}g
            </span>
        </div>

        <div className="flex justify-between text-xl">
            <span>Total Price</span>

            <span className="font-bold text-green-700">
            ₹{totalPrice}
            </span>
        </div>
    </div>

    {remainingAmount > 0 ? (
      <div className="mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">
        Add ₹{remainingAmount} more to reach
        the minimum order value of ₹{MIN_ORDER_VALUE}
      </div>
    ) : (
      <div className="mt-6 rounded-xl border border-green-200 bg-green-50 p-4 text-green-700">
        ✅ Minimum order reached
      </div>
    )}
  </div>
 );
}
