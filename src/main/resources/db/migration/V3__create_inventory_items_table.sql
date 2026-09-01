CREATE TABLE inventory_items (
    product_id NUMBER(19) PRIMARY KEY,
    available NUMBER(12) NOT NULL CHECK(available >= 0),
    reserved NUMBER(12) NOT NULL CHECK(reserved >= 0),
    CONSTRAINT fk_inventory_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);