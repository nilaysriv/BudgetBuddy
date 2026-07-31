package com.nilay.budgetbuddy.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps a Category's iconKey to a real icon. Falls back to a generic category icon for unknown keys. */
object CategoryIcons {
    val icons: Map<String, ImageVector> = mapOf(
        "restaurant" to Icons.Rounded.Restaurant,
        "directions_bus" to Icons.Rounded.DirectionsBus,
        "home" to Icons.Rounded.Home,
        "bolt" to Icons.Rounded.Bolt,
        "shopping_cart" to Icons.Rounded.ShoppingCart,
        "movie" to Icons.Rounded.Movie,
        "medical_services" to Icons.Rounded.MedicalServices,
        "payments" to Icons.Rounded.Payments,
        "more_horiz" to Icons.Rounded.MoreHoriz,
        "groceries" to Icons.Rounded.LocalGroceryStore,
        "dining" to Icons.Rounded.LocalDining,
        "fuel" to Icons.Rounded.LocalGasStation,
        "rent" to Icons.Rounded.Apartment,
        "entertainment" to Icons.Rounded.TheaterComedy,
        "health" to Icons.Rounded.HealthAndSafety,
        "education" to Icons.Rounded.School,
        "travel" to Icons.Rounded.Flight,
        "gifts" to Icons.Rounded.CardGiftcard,
        "pets" to Icons.Rounded.Pets,
        "subscriptions" to Icons.Rounded.Subscriptions,
        "savings" to Icons.Rounded.Savings,
        "salary" to Icons.Rounded.Work,
        "freelance" to Icons.Rounded.Laptop,
        "investment" to Icons.AutoMirrored.Rounded.TrendingUp,
        "phone" to Icons.Rounded.PhoneAndroid,
        "fitness" to Icons.Rounded.FitnessCenter,
        "coffee" to Icons.Rounded.LocalCafe,
        "insurance" to Icons.Rounded.Shield,
        "kids" to Icons.Rounded.ChildCare,
        "other" to Icons.Rounded.Category
    )

    fun iconFor(key: String): ImageVector = icons[key] ?: Icons.Rounded.Category
}
