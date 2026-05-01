SELECT 'CREATE DATABASE ecommerce_user_service'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_user_service')\gexec

SELECT 'CREATE DATABASE ecommerce_product_service'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_product_service')\gexec

SELECT 'CREATE DATABASE ecommerce_cart_service'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_cart_service')\gexec

SELECT 'CREATE DATABASE ecommerce_order_service'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_order_service')\gexec

SELECT 'CREATE DATABASE ecommerce_payment_service'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_payment_service')\gexec
