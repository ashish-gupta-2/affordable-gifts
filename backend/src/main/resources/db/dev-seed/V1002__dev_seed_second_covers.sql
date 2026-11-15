-- A second cover per layout, so the dev storefront shows a real choice.
insert into packaging_options (id, name, kind, description, price, slot_count, slot_capacity, display_order)
values ('cover-3x100-gold', 'Gold Cover (3 x 100g)', 'cover', 'Holds 3 jars of 100g', 35, 3, 100, 92),
       ('cover-2x200-gold', 'Gold Cover (2 x 200g)', 'cover', 'Holds 2 jars of 200g', 35, 2, 200, 93)
on conflict do nothing;
