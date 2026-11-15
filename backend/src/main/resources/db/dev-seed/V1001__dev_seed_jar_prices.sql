-- Dev prices after jars and covers became separate items. Set explicitly (not divided) so
-- it gives the same result whether or not V4 already converted the jar rows.
-- Totals stay as before: 3 x 100g jars = 3 x 20 + 20 = 80, 2 x 200g jars = 2 x 35 + 20 = 90.
update packaging_options set price = 20 where id = 'jar-100';
update packaging_options set price = 35 where id = 'jar-200';
update packaging_options set price = 20 where id in ('cover-3x100', 'cover-2x200');
