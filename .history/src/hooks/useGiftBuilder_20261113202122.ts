import { useMemo, useState } from "react";
import type { CartItem, Product } from "../types";

const MIN_ORDER_VALUE = 299;

export function useGiftBuilder() {
  const [items, setItems] = useState<CartItem[]>([]);

  const addProduct = (product: Product) => {
    setItems((current) => {
      const existing = current.find(
        (item) => item.product.id === product.id
      );

      if (existing) {
        return current.map((item) =>
          item.product.id === product.id
            ? {
                ...item,
                quantity: item.quantity + 1,
              }
            : item
        );
      }

      return [
        ...current,
        {
          product,
          quantity: 1,
        },
      ];
    });
  };

  
  const totalPrice = useMemo(
    () =>
      items.reduce(
        (sum, item) =>
          sum + item.product.price * item.quantity,
        0
      ),
    [items]
  );

  const totalWeight = useMemo(
    () =>
      items.reduce(
        (sum, item) =>
          sum + item.product.weight * item.quantity,
        0
      ),
    [items]
  );

  const remainingAmount = Math.max(
    0,
    MIN_ORDER_VALUE - totalPrice
  );

  return {
    items,
    addProduct,
    totalPrice,
    totalWeight,
    remainingAmount,
  };
}