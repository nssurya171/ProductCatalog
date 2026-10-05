package com.surya.productcatalog.ui.components

import java.util.Locale

/** Formats a price like "$12.99". Locale.US keeps the decimal point consistent. */
fun formatPrice(price: Double): String = String.format(Locale.US, "$%.2f", price)

/** Formats a rating like "4.5". */
fun formatRating(rating: Double): String = String.format(Locale.US, "%.1f", rating)
