ALTER TABLE "SHOPPING_LISTS" RENAME TO "SHOPPING_LISTS_TEMP";
CREATE TABLE "SHOPPING_LISTS" ("_id" INTEGER PRIMARY KEY  NOT NULL ,"NAME" TEXT, "IS_FILTERED" BOOL DEFAULT 0);
INSERT INTO "SHOPPING_LISTS" ("_id" ,"NAME", "IS_FILTERED") SELECT "_id" ,"NAME", 0 FROM "SHOPPING_LISTS_TEMP";
DROP TABLE "SHOPPING_LISTS_TEMP";

INSERT INTO "CATEGORIES" ("CATEGORY_NAME", "CATEGORY_ORDER", "CATEGORY_PICTURE_URI") 
VALUES 
("Household chemicals", "56", "drawable/category_household_chemicals"),
("Pills", "57", "drawable/category_pills"),
("Hygienic means", "58", "drawable/category_hygienic_means"),
("Electronics", "59", "drawable/category_electronics"),
("Car accessories", "60", "drawable/category_car");

UPDATE "CATEGORIES" SET "CATEGORY_ORDER" = "90" WHERE "CATEGORY_ID" = "10"
