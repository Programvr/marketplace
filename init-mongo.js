// Crear base de datos
db = db.getSiblingDB('marketplace_logs');

// Crear colecciones
db.createCollection('activity_logs');
db.createCollection('shopping_carts');

// Crear índices para mejor rendimiento
db.activity_logs.createIndex({ "createdAt": -1 });
db.activity_logs.createIndex({ "userId": 1 });
db.activity_logs.createIndex({ "action": 1 });
db.activity_logs.createIndex({ "entityType": 1 });
db.activity_logs.createIndex({ "level": 1 });

db.shopping_carts.createIndex({ "userId": 1 }, { unique: true });
db.shopping_carts.createIndex({ "expiresAt": 1 }, { expireAfterSeconds: 0 });

// Insertar documento de prueba
db.activity_logs.insertOne({
    message: "MongoDB initialized successfully",
    level: "INFO",
    createdAt: new Date(),
    service: "marketplace-api",
    environment: "docker"
});

print("=========================================");
print("MongoDB initialized successfully!");
print("Database: marketplace_logs");
print("Collections: activity_logs, shopping_carts");
print("=========================================");