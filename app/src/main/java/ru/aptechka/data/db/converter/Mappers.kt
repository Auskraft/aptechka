package ru.aptechka.data.db.converter

import ru.aptechka.data.db.entity.*
import ru.aptechka.domain.model.*

fun KitEntity.toDomain() = Kit(
    id = id,
    name = name,
    description = description,
    iconName = iconName,
    colorHex = colorHex,
    createdAt = createdAt
)

fun Kit.toEntity() = KitEntity(
    id = id,
    name = name,
    description = description,
    iconName = iconName,
    colorHex = colorHex,
    createdAt = createdAt
)

fun UserDrugEntity.toDomain() = UserDrug(
    id = id,
    kitId = kitId,
    catalogDrugId = catalogDrugId,
    name = name,
    form = FormKey.valueOf(form),
    unit = UnitKey.valueOf(unit),
    category = CategoryKey.valueOf(category),
    notes = notes,
    imageUri = imageUri,
    createdAt = createdAt
)

fun UserDrug.toEntity() = UserDrugEntity(
    id = id,
    kitId = kitId,
    catalogDrugId = catalogDrugId,
    name = name,
    form = form.name,
    unit = unit.name,
    category = category.name,
    notes = notes,
    imageUri = imageUri,
    createdAt = createdAt
)

fun DrugBatchEntity.toDomain() = DrugBatch(
    id = id,
    drugId = drugId,
    quantity = quantity,
    expirationDate = expirationDate,
    purchaseDate = purchaseDate,
    price = price,
    barcode = barcode,
    notes = notes
)

fun DrugBatch.toEntity() = DrugBatchEntity(
    id = id,
    drugId = drugId,
    quantity = quantity,
    expirationDate = expirationDate,
    purchaseDate = purchaseDate,
    price = price,
    barcode = barcode,
    notes = notes
)

fun ShoppingItemEntity.toDomain() = ShoppingItem(
    id = id,
    name = name,
    quantity = quantity,
    unit = UnitKey.valueOf(unit),
    kitId = kitId,
    catalogDrugId = catalogDrugId,
    isPurchased = isPurchased,
    createdAt = createdAt
)

fun ShoppingItem.toEntity() = ShoppingItemEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit.name,
    kitId = kitId,
    catalogDrugId = catalogDrugId,
    isPurchased = isPurchased,
    createdAt = createdAt
)

fun ReminderEntity.toDomain() = Reminder(
    id = id,
    drugId = drugId,
    kitId = kitId,
    title = title,
    message = message,
    daysBeforeExpiry = daysBeforeExpiry,
    isGlobal = isGlobal,
    isEnabled = isEnabled
)

fun Reminder.toEntity() = ReminderEntity(
    id = id,
    drugId = drugId,
    kitId = kitId,
    title = title,
    message = message,
    daysBeforeExpiry = daysBeforeExpiry,
    isGlobal = isGlobal,
    isEnabled = isEnabled
)

fun CatalogDrugEntity.toDomain() = CatalogDrug(
    id = id,
    name = name,
    internationalName = internationalName,
    manufacturer = manufacturer,
    form = FormKey.valueOf(form),
    unit = UnitKey.valueOf(unit),
    category = CategoryKey.valueOf(category),
    barcode = barcode,
    description = description,
    storageConditions = storageConditions,
    sideEffects = sideEffects,
    contraindications = contraindications
)