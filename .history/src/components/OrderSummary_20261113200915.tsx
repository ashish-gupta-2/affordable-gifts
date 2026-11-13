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
    <div className="sticky top-6 rounded-xl bg-white p-6 shadow">
      <h2 className="mb-4 text-xl font-bold">
        Order Summary
      </h2>

      <div className="space-y-2">
        {items.map((item) => (
          <div
            key={item.product.id}
            className="flex justify-between"
          >
            <span>
              {item.product.name} x {item.quantity}
            </span>

            <span>
              ₹
              {item.product.price *
                item.quantity}
            </span>
          </div>
        ))}
      </div>

      <hr className="my-4" />

      <p>Total Weight: {totalWeight}g</p>

      <p className="font-bold">
        Total Price: ₹{totalPrice}
      </p>

      {remainingAmount > 0 ? (
        <p className="mt-3 text-sm text-red-600">
          Add ₹{remainingAmount} more to reach
          minimum order value.
        </p>
      ) : (
        <p className="mt-3 text-green-600">
          Minimum order reached ✅
        </p>
      )}
    </div>
  );
}