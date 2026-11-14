export interface Product {
  id: string;
  name: string;
  category: "Dry Fruits" | "Snacks";
  weight: number;
  price: number;
  image: string;
}

export interface Packaging {
  id: string;
  name: string;
  price: number;
  maxWeight: number;
}

export interface GreetingCard {
  id: string;
  name: string;
  price: number;
}

export interface CartItem {
  product: Product;
  quantity: number;
}