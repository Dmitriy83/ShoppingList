ALTER TABLE "SHOPPING_LISTS" RENAME TO "SHOPPING_LISTS_TEMP";
CREATE TABLE "SHOPPING_LISTS" ("_id" INTEGER PRIMARY KEY  NOT NULL ,"NAME" TEXT, "IS_FILTERED" BOOL DEFAULT 0);
INSERT INTO "SHOPPING_LISTS" ("_id" ,"NAME", "IS_FILTERED") SELECT "_id" ,"NAME", 0 FROM "SHOPPING_LISTS_TEMP";
DROP TABLE "SHOPPING_LISTS_TEMP";

INSERT INTO "CATEGORIES" ("CATEGORY_NAME", "CATEGORY_ORDER", "CATEGORY_PICTURE_URI") 
VALUES 
("Бытовая химия", "56", "drawable/category_household_chemicals"),
("Медикаменты", "57", "drawable/category_pills"),
("Средства гигиены", "58", "drawable/category_hygienic_means"),
("Электроника", "59", "drawable/category_electronics"),
("Акссесуары для машины", "60", "drawable/category_car");

UPDATE "CATEGORIES" SET "CATEGORY_ORDER" = "90" WHERE "CATEGORY_ID" = "10"