-- Glass jar covers are their own priced item, in the two standard sizes. A jar set
-- costs (price per jar x number of jars) + cover price.
alter table packaging_options drop constraint packaging_options_kind_check;
alter table packaging_options add constraint packaging_options_kind_check
    check (kind in ('box', 'jar', 'cover'));

-- Existing jar rows held the whole set price; make it a per-jar price. Review prices in /admin.
update packaging_options set price = round(price / slot_count, 2) where kind = 'jar';

-- The two standard covers. Cover prices start at 0: set them in /admin.
insert into packaging_options (id, name, kind, description, price, slot_count, slot_capacity, display_order)
values ('cover-3x100', 'Glass Jar Cover (3 x 100g)', 'cover', 'Holds 3 jars of 100g', 0, 3, 100, 90),
       ('cover-2x200', 'Glass Jar Cover (2 x 200g)', 'cover', 'Holds 2 jars of 200g', 0, 2, 200, 91);
