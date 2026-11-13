import type { CartItem } from "../types";

interface Props {
  items: CartItem[];
  totalPrice: number;
  totalWeight: number;
  remainingAmount: number;
}

export default function OrderSummary({
  items,
  totalPrice,
  totalWeight,
  remainingAmount,
}: Props) {
  return (
    <div className="rounded-xl bg-white p-6 shadow">
      <h2 className="mb-4 text-xl font-bold">
        Order Summary
      </h2>

      {items.length === 0 ? (
        <p className="text-gray-500">
          No products added yet
        </p>
      ) : (
        <div className="space-y-2">
          {items.map((item) => (
            <div
              key={item.product.id}
              className="flex justify-between"
            >
              <span>
                {item.product.name} × {item.quantity}
              </span>

              <span>
                ₹
                {item.product.price *
                  item.quantity}
              </span>
            </div>
          ))}
        </div>
      )}

      <hr className="my-4" />

      <div className="space-y-2">
        <p>
          Total Weight: <b>{totalWeight}g</b>
        </p>

        <p>
          Total Price: <b>₹{totalPrice}</b>
        </p>
      </div>

      {remainingAmount > 0 ? (
        <div className="mt-4 rounded-lg bg-red-50 p-3 text-red-600">
          Add ₹{remainingAmount} more to
          reach the minimum order value.
        </div>
      ) : (
        <div className="mt-4 rounded-lg bg-green-50 p-3 text-green-700">
          Minimum order reached ✅
        </div>
      )}
    </div>
  );
}
