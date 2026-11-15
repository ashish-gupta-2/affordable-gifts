import { useState } from "react";
import type { Packaging, Product } from "../types";

/** One filled partition / jar: a single product at the slot's pack size. */
export interface SlotFill {
  product: Product;
  weight: number;
  price: number;
}

export type Slot = SlotFill | null;

/** `minOrderValue` comes from the backend settings (admin-configurable). */
export function useGiftBuilder(minOrderValue: number) {
  const [selectedPackaging, setSelectedPackaging] =
    useState<Packaging | null>(null);
  // Glass jars also need an outer cover; boxes do not.
  const [selectedCover, setSelectedCover] = useState<Packaging | null>(null);
  const [slots, setSlots] = useState<Slot[]>([]);
  // `slots` describes ONE box / jar set. Every copy is identical, so the
  // quantity simply multiplies the set (price, weight, packaging).
  const [packagingQuantity, setPackagingQuantity] = useState(1);

  const selectPackaging = (packaging: Packaging) => {
    // Keep already-picked products only if the slot size is unchanged.
    const keepFills =
      selectedPackaging?.slotCapacity === packaging.slotCapacity;

    setSelectedPackaging(packaging);
    // A cover only fits the jar layout it was made for.
    setSelectedCover((cover) =>
      packaging.kind === "jar" &&
      cover?.slotCount === packaging.slotCount &&
      cover.slotCapacity === packaging.slotCapacity
        ? cover
        : null
    );
    setSlots((current) =>
      Array.from(
        { length: packaging.slotCount },
        (_, index) => (keepFills ? current[index] ?? null : null)
      )
    );
  };

  const selectCover = (cover: Packaging) => setSelectedCover(cover);

  const increasePackaging = () => {
    if (!selectedPackaging) return;
    setPackagingQuantity((current) => current + 1);
  };

  const decreasePackaging = () => {
    setPackagingQuantity((current) => Math.max(1, current - 1));
  };

  /** Why this product can't be added right now, or null if it can. */
  const getAddBlockReason = (product: Product): string | null => {
    if (!selectedPackaging) return "Choose packaging first";
    if (!slots.includes(null)) return "Box is full";
    const hasPack = product.packs.some(
      (pack) => pack.weight === selectedPackaging.slotCapacity
    );
    if (!hasPack) return `No ${selectedPackaging.slotCapacity}g pack`;
    return null;
  };

  const addProduct = (product: Product) => {
    if (!selectedPackaging || getAddBlockReason(product)) return;

    const pack = product.packs.find(
      (p) => p.weight === selectedPackaging.slotCapacity
    );
    if (!pack) return;

    setSlots((current) => {
      const index = current.indexOf(null);
      if (index === -1) return current;

      const next = [...current];
      next[index] = {
        product,
        weight: pack.weight,
        price: pack.price,
      };
      return next;
    });
  };

  const clearSlot = (index: number) => {
    setSlots((current) =>
      current.map((slot, i) => (i === index ? null : slot))
    );
  };

  const productTotal =
    slots.reduce((sum, slot) => sum + (slot?.price ?? 0), 0) *
    packagingQuantity;

  const totalWeight =
    slots.reduce((sum, slot) => sum + (slot?.weight ?? 0), 0) *
    packagingQuantity;

  const emptySlotCount = slots.filter((slot) => slot === null).length;

  const needsCover = selectedPackaging?.kind === "jar";

  /** One box, or one jar set: the jars plus the chosen cover. */
  const setPackagingPrice = !selectedPackaging
    ? 0
    : needsCover
      ? selectedPackaging.price * selectedPackaging.slotCount +
        (selectedCover?.price ?? 0)
      : selectedPackaging.price;

  const packagingPrice = setPackagingPrice * packagingQuantity;

  const totalPrice = productTotal + packagingPrice;

  const reset = () => {
    setSelectedPackaging(null);
    setSelectedCover(null);
    setSlots([]);
    setPackagingQuantity(1);
  };

  const remainingAmount = Math.max(0, minOrderValue - productTotal);

  const canCheckout =
    selectedPackaging !== null &&
    (!needsCover || selectedCover !== null) &&
    emptySlotCount === 0 &&
    remainingAmount === 0;

  return {
    selectedPackaging,
    selectPackaging,
    selectedCover,
    selectCover,
    needsCover,
    setPackagingPrice,
    packagingQuantity,
    increasePackaging,
    decreasePackaging,
    packagingPrice,
    slots,
    addProduct,
    clearSlot,
    getAddBlockReason,
    productTotal,
    totalPrice,
    totalWeight,
    remainingAmount,
    emptySlotCount,
    canCheckout,
    reset,
  };
}

export type GiftBuilder = ReturnType<typeof useGiftBuilder>;
