#!/bin/bash

echo "Kalan repository testlerini düzeltiliyor..."

# OrderRepositoryTest
sed -i '' 's/Brand testBrand = Brand.builder()/Dealer testDealer = Dealer.builder()\
                .name("Test Dealer")\
                .active(true)\
                .build();\
        entityManager.persist(testDealer);\
        entityManager.flush();\
\
        Brand testBrand = Brand.builder()\
                .dealer(testDealer)/' src/test/java/com/baskiliisler/backend/repository/OrderRepositoryTest.java

# QuoteRepositoryTest
sed -i '' 's/Brand testBrand = Brand.builder()/Dealer testDealer = Dealer.builder()\
                .name("Test Dealer")\
                .active(true)\
                .build();\
        entityManager.persist(testDealer);\
        entityManager.flush();\
\
        Brand testBrand = Brand.builder()\
                .dealer(testDealer)/' src/test/java/com/baskiliisler/backend/repository/QuoteRepositoryTest.java

# QuoteItemRepositoryTest
sed -i '' 's/Brand testBrand = Brand.builder()/Dealer testDealer = Dealer.builder()\
                .name("Test Dealer")\
                .active(true)\
                .build();\
        entityManager.persist(testDealer);\
        entityManager.flush();\
\
        Brand testBrand = Brand.builder()\
                .dealer(testDealer)/' src/test/java/com/baskiliisler/backend/repository/QuoteItemRepositoryTest.java

echo "Repository testleri düzeltildi!"